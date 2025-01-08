package com.yfckevin.bingBao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.*;
import com.yfckevin.bingBao.entity.Follower;
import com.yfckevin.bingBao.entity.Inventory;
import com.yfckevin.bingBao.entity.Product;
import com.yfckevin.bingBao.entity.ReceiveItem;
import com.yfckevin.bingBao.enums.PackageForm;
import com.yfckevin.bingBao.enums.StorePlace;
import com.yfckevin.bingBao.exception.ResultStatus;
import com.yfckevin.bingBao.service.*;
import com.yfckevin.bingBao.utils.FileUtils;
import com.yfckevin.bingBao.utils.JwtTool;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yfckevin.bingBao.utils.DateUtil.genDateFormatted;
import static com.yfckevin.bingBao.utils.DateUtil.genNoticeDateFormatted;

@Controller
public class InventoryController {
    Logger logger = LoggerFactory.getLogger(InventoryController.class);
    private final SimpleDateFormat sdf;
    private final ConfigProperties configProperties;
    private final InventoryService inventoryService;
    private final ReceiveItemService receiveItemService;
    private final ProductService productService;
    private final DataProcessService dataProcessService;
    private final RestTemplate restTemplate;
    private final RecordService recordService;
    private final ObjectMapper objectMapper;
    private final FollowerService followerService;
    private final JwtTool jwtTool;

    public InventoryController(@Qualifier("sdf") SimpleDateFormat sdf, ConfigProperties configProperties, InventoryService inventoryService, ReceiveItemService receiveItemService, ProductService productService, DataProcessService dataProcessService, RestTemplate restTemplate, RecordService recordService, ObjectMapper objectMapper, FollowerService followerService, JwtTool jwtTool) {
        this.sdf = sdf;
        this.configProperties = configProperties;
        this.inventoryService = inventoryService;
        this.receiveItemService = receiveItemService;
        this.productService = productService;
        this.dataProcessService = dataProcessService;
        this.restTemplate = restTemplate;
        this.recordService = recordService;
        this.objectMapper = objectMapper;
        this.followerService = followerService;
        this.jwtTool = jwtTool;
    }

    /**
     * 使用食材對庫存數量進行扣減
     *
     * @param session
     * @return
     */
    @PostMapping("/editAmountInventory")
    public ResponseEntity<?> editAmountInventory(@RequestBody UseRequestDTO dto, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        final String memberName = dto.getMemberName();
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[editAmountInventory]");
        } else if (StringUtils.isNotBlank(memberName)) {
            logger.info("[" + memberName + "]" + "[editAmountInventory]");
        }
        ResultStatus resultStatus = new ResultStatus();

        List<Inventory> inventoryList = inventoryService.findByReceiveItemIdAndUsedDateIsNullAndDeletionDateIsNull(dto.getReceiveItemId());
        if (inventoryList.size() == 0) {
            resultStatus.setCode("C006");
            resultStatus.setMessage("無庫存");
            return ResponseEntity.ok(resultStatus);
        }

        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        int amountToUse = dto.getUsedAmount();
        for (Inventory inventory : inventoryList) {
            inventory.setUsedDate(sdf.format(new Date()));
            if (StringUtils.isNotBlank(memberName)) {
                inventory.setModifier(memberName);
            } else if (member != null) {
                inventory.setModifier(member.getName());
            }
            inventory.setModificationDate(sdf.format(new Date()));
            inventoriesToUpdate.add(inventory);
            amountToUse--;

            if (amountToUse <= 0){
                break;
            }
        }
        if (!inventoriesToUpdate.isEmpty()) {
            final List<Inventory> saveInventoryList = inventoryService.saveAll(inventoriesToUpdate);
            recordService.editInventoryAmount(saveInventoryList, dto.getUsedAmount());
        }

        if (amountToUse > 0) {
            resultStatus.setCode("C007");
            resultStatus.setMessage("庫存不足");
        } else {
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        }

        dataProcessService.inventoryDataProcess(inventoriesToUpdate);

        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 檢查該食材是否有庫存 (line點擊「修改剩餘數量」用)
     * @param receiveItemId
     * @param productId
     * @param memberId
     * @param response
     * @throws IOException
     */
    @GetMapping("/checkInventory")
    public void checkInventory(@RequestParam("receiveItemId") String receiveItemId,
                               @RequestParam("productId") String productId,
                               @RequestParam("memberId") String memberId,
                               @RequestParam("token") String token,
                               HttpServletResponse response) throws IOException {
        try {
            jwtTool.parseToken(token);

            final Optional<Follower> followerOpt = followerService.findByUserId(memberId);
            if (followerOpt.isPresent()) {
                final Follower follower = followerOpt.get();
                String memberName = follower.getDisplayName();
                logger.info("[" + memberName + "]" + "[checkInventory]");
            }

            final long oldAmount = inventoryService.findByReceiveItemId(receiveItemId).stream()
                    .filter(inventory -> StringUtils.isBlank(inventory.getUsedDate()) &&
                            StringUtils.isBlank(inventory.getDeletionDate()) &&
                            !LocalDate.now().isAfter(LocalDate.parse(inventory.getExpiryDate())))
                    .count();

            String url = configProperties.getGlobalDomain() + "edit-amount-page.html" +
                    "?receiveItemId=" + receiveItemId +
                    "&productId=" + productId +
                    "&memberId=" + memberId +
                    "&oldAmount=" + oldAmount;

            response.sendRedirect(url);

        } catch (RuntimeException e) {

            String url = configProperties.getGlobalDomain() + "error.html?token=expired";

            response.sendRedirect(url);

        }
    }


    /**
     * 新增食材庫存以及更新收貨明細
     * @param inventoryId
     * @param cloneAmount
     * @param session
     * @return
     */
    @GetMapping("/cloneInventory/{inventoryId}/{cloneAmount}")
    public ResponseEntity<?> cloneInventory (@PathVariable String inventoryId, @PathVariable int cloneAmount, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[cloneInventory]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final Optional<Inventory> inventoryOpt = inventoryService.findById(inventoryId);
        if (inventoryOpt.isEmpty()) {
            resultStatus.setCode("C006");
            resultStatus.setMessage("無庫存");
        } else {
            final Inventory inventory = inventoryOpt.get();
            //更新receiveItem
            Optional<ReceiveItem> receiveItemOpt = receiveItemService.findById(inventory.getReceiveItemId());
            if (receiveItemOpt.isEmpty()) {
                resultStatus.setCode("C013");
                resultStatus.setMessage("查無收貨明細");
            } else {
                final ReceiveItem receiveItem = receiveItemOpt.get();
                final Optional<Product> productOpt = productService.findById(receiveItem.getProductId());
                if (productOpt.isEmpty()) {
                    resultStatus.setCode("C001");
                    resultStatus.setMessage("查無食材");
                    return ResponseEntity.ok(resultStatus);
                } else {
                    final Product product = productOpt.get();
                    if (PackageForm.COMPLETE.equals(product.getPackageForm())) {
                        receiveItem.setTotalAmount((receiveItem.getAmount() + cloneAmount) * Integer.parseInt(product.getPackageQuantity()));
                    } else {
                        receiveItem.setTotalAmount(receiveItem.getAmount() + cloneAmount);
                    }
                    receiveItem.setAmount(receiveItem.getAmount() + cloneAmount);
                    receiveItem.setModificationDate(sdf.format(new Date()));
                    receiveItem.setModifier(member.getName());
                    receiveItemService.save(receiveItem);

                    //產生食材庫存
                    List<Inventory> cloneInventoryList = new ArrayList<>();

                    cloneAmount = PackageForm.COMPLETE.equals(product.getPackageForm())
                            ? cloneAmount * Integer.parseInt(product.getPackageQuantity())
                            : cloneAmount;

                    for (int i = 0; i < cloneAmount; i++) {
                        final Inventory cloneInventory = inventory.clone();
                        cloneInventory.setId(null);
                        cloneInventory.setCreationDate(sdf.format(new Date()));
                        cloneInventory.setCreator(member.getName());
                        cloneInventoryList.add(cloneInventory);
                    }

                    final List<Inventory> inventoryList = inventoryService.saveAll(cloneInventoryList);
                    recordService.cloneInventory(inventoryList, cloneAmount);
                }
                resultStatus.setCode("C000");
                resultStatus.setMessage("成功");
            }
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 修改食材庫存的有效期限
     * @param session
     * @return
     */
    @PostMapping("/editExpiryDate")
    public ResponseEntity<?> editExpiryDate (@RequestBody ExpiryDateRequestDTO dto, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        final String memberName = dto.getMemberName();
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[editExpiryDate]");
        } else {
            if (StringUtils.isNotBlank(memberName)) {
                logger.info("[" + memberName + "]" + "[editExpiryDate]");
            }
        }
        ResultStatus resultStatus = new ResultStatus();

        List<Inventory> inventoryList = inventoryService.findByReceiveItemId(dto.getReceiveItemId())
                .stream()
                .filter(inventory -> inventory.getDeletionDate() == null)
                .toList();
        if (inventoryList.size() == 0) {
            resultStatus.setCode("C006");
            resultStatus.setMessage("無庫存");
        } else {
            inventoryList = inventoryList.stream()
                    .peek(inventory -> {
                        inventory.setExpiryDate(dto.getExpiryDate());
                        inventory.setModificationDate(sdf.format(new Date()));
                        if (StringUtils.isNotBlank(memberName)) {
                            inventory.setModifier(memberName);
                        } else if (member != null) {
                            inventory.setModifier(member.getName());
                        }
                    }).toList();
            final List<Inventory> saveInventoryList = inventoryService.saveAll(inventoryList);
            recordService.editInventoryExpiryDate(saveInventoryList, dto.getExpiryDate());
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 變更庫存食材的存放位置
     * @param dto
     * @param session
     * @return
     */
    @PostMapping("/editStorePlaceInventory")
    public ResponseEntity<?> editStorePlaceInventory(@RequestBody ChangeStorePlaceRequestDTO dto, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[editStorePlaceInventory]");
        }
        ResultStatus resultStatus = new ResultStatus();

        // 只比對沒用過、沒刪除以及沒過期的食材庫存
        List<Inventory> inventoryList = inventoryService.findByReceiveItemIdAndForEditStorePlaceInventory(dto.getReceiveItemId());

        if (inventoryList.size() == 0) {
            resultStatus.setCode("C006");
            resultStatus.setMessage("無庫存");
            return ResponseEntity.ok(resultStatus);
        }

        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        final String newStorePlace = dto.getNewStorePlace();
        for (Inventory inventory : inventoryList) {
            inventory.setStorePlace(StorePlace.valueOf(newStorePlace));
            inventory.setModifier(member.getName());
            inventory.setModificationDate(sdf.format(new Date()));
            inventoriesToUpdate.add(inventory);
        }
        if (!inventoriesToUpdate.isEmpty()) {
            final List<Inventory> saveInventoryList = inventoryService.saveAll(inventoriesToUpdate);
            recordService.editInventoryStorePlace(saveInventoryList, dto.getNewStorePlace());
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        }

        dataProcessService.inventoryDataProcess(inventoriesToUpdate);

        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/deleteInventory/{id}")
    public ResponseEntity<?> deleteInventory (@PathVariable String id, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[deleteInventory]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final List<Inventory> inventoryList = inventoryService.findByReceiveItemIdAndUsedDateIsNullAndDeletionDateIsNull(id);
        if (inventoryList.size() == 0) {
            resultStatus.setCode("C006");
            resultStatus.setMessage("無庫存");
            return ResponseEntity.ok(resultStatus);
        }
        final List<Inventory> inventoriesToDelete = inventoryList.stream().peek(i -> i.setDeletionDate(sdf.format(new Date()))).toList();
        final List<Inventory> saveInventoryList = inventoryService.saveAll(inventoriesToDelete);
        recordService.deleteInventory(saveInventoryList, member);

        dataProcessService.inventoryDataProcess(inventoriesToDelete);

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 查詢單一庫存食材資訊
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/getInventoryInfo/{id}")
    public ResponseEntity<?> getInventoryInfo (@PathVariable String id, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[getInventoryInfo]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final Optional<Inventory> opt = inventoryService.findById(id);
        if (opt.isEmpty()) {
            resultStatus.setCode("C008");
            resultStatus.setMessage("查無庫存資料");
        } else {
            final Inventory inventory = opt.get();
            final Optional<Product> productOpt = productService.findById(inventory.getProductId());
            if (productOpt.isEmpty()) {
                resultStatus.setCode("C001");
                resultStatus.setMessage("查無食材");
            } else {
                final Product product = productOpt.get();
                final InventoryDTO inventoryDTO = constructInventoryDTO(inventory, product, null);
                resultStatus.setCode("C000");
                resultStatus.setMessage("成功");
                resultStatus.setData(inventoryDTO);
            }
        }
        return ResponseEntity.ok(resultStatus);
    }



    @PostMapping("/dashboard")
    public ResponseEntity<?> dashboard(@RequestBody List<SearchDTO> searchCondition, HttpSession session) {
        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[dashboard]");
        }
        ResultStatus resultStatus = new ResultStatus();
        Map<String, List<InventoryDTO>> inventoryMap = new HashMap<>();

        String url = configProperties.getGlobalDomain() + "inventorySearch";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            for (String type : new String[] { "today", "soon", "valid" }) {
                SearchDTO searchDTO = searchCondition.stream()
                        .filter(dto -> type.equals(dto.getType()))
                        .findFirst()
                        .orElse(new SearchDTO(type));

                HttpPost post = new HttpPost(url);
                post.setHeader("Internal-Request", "true");
                post.setHeader("Content-Type", "application/json");
                StringEntity entity = new StringEntity(objectMapper.writeValueAsString(searchDTO), ContentType.APPLICATION_JSON);
                post.setEntity(entity);

                try (CloseableHttpResponse response = client.execute(post)) {
                    if (response.getCode() == 200) {
                        ResultStatus<List<InventoryDTO>> result = objectMapper.readValue(
                                response.getEntity().getContent(),
                                objectMapper.getTypeFactory().constructParametricType(ResultStatus.class, List.class)
                        );
                        if ("C000".equals(result.getCode())) {
                            inventoryMap.put(type, result.getData());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching inventory", e);
        }

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(inventoryMap);
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/inventorySearch")
    public ResponseEntity<?> inventorySearch(@RequestBody SearchDTO searchDTO, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[inventorySearch]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final String keyword = searchDTO.getKeyword().trim();
        final String mainCategory = searchDTO.getMainCategory();
        final String subCategory = searchDTO.getSubCategory();
        final String type = searchDTO.getType();
        final String storePlace = searchDTO.getStorePlace();
        final String supplierId = searchDTO.getSupplierId();
        System.out.println(keyword + " / " + mainCategory + " / " + subCategory + " / " + storePlace + " / " + supplierId);
        Map<String, Map<Long, List<Inventory>>> itemIdAmountInventoryMap = new HashMap<>();
        if (StringUtils.isNotBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 只有輸入名稱
            itemIdAmountInventoryMap = inventoryService.searchByName(keyword, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 有輸入名稱 + 只有主種類
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategory(keyword, mainCategory, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 只有主種類
            itemIdAmountInventoryMap = inventoryService.searchByMainCategory(mainCategory, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 有輸入名稱 + 有主種類 + 有副種類
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndSubCategory(keyword, mainCategory, subCategory, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 有主種類 + 有副種類
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndSubCategory(mainCategory, subCategory, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 輸入名稱 + 存放位置
            itemIdAmountInventoryMap = inventoryService.searchByNameAndStorePlace(keyword, type, storePlace);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 有輸入名稱 + 只有主種類 + 存放位置
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndStorePlace(keyword, mainCategory, type, storePlace);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 主種類 + 存放位置
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndStorePlace(mainCategory, type, storePlace);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 有輸入名稱 + 有主種類 + 有副種類 + 存放位置
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndSubCategoryAndStorePlace(keyword, mainCategory, subCategory, type, storePlace);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 有主種類 + 有副種類 + 存放位置
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndSubCategoryAndStorePlace(mainCategory, subCategory, type, storePlace);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isBlank(supplierId)) {
            // 只有存放位置
            itemIdAmountInventoryMap = inventoryService.searchByStorePlace(type, storePlace);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 只有輸入名稱 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByNameAndSupplierId(keyword, supplierId, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 有輸入名稱 + 只有主種類 + 應商名
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndSupplierId(keyword, mainCategory, supplierId, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 只有主種類 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndSupplierId(mainCategory, supplierId, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 有輸入名稱 + 有主種類 + 有副種類 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndSubCategoryAndSupplierId(keyword, mainCategory, subCategory, supplierId, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 有主種類 + 有副種類 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndSubCategoryAndSupplierId(mainCategory, subCategory, supplierId, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 輸入名稱 + 存放位置 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByNameAndStorePlaceAndSupplierId(keyword, storePlace, supplierId, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 有輸入名稱 + 只有主種類 + 存放位置 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndStorePlaceAndSupplierId(keyword, mainCategory, storePlace, supplierId, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 主種類 + 存放位置 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndStorePlaceAndSupplierId(mainCategory, storePlace, supplierId, type);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 有輸入名稱 + 有主種類 + 有副種類 + 存放位置 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByNameAndMainCategoryAndSubCategoryAndStorePlaceAndSupplierId(keyword, mainCategory, subCategory, storePlace, supplierId, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 有主種類 + 有副種類 + 存放位置 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByMainCategoryAndSubCategoryAndStorePlaceAndSupplierId(mainCategory, subCategory, storePlace, supplierId, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isNotBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 只有存放位置 + 供應商名
            itemIdAmountInventoryMap = inventoryService.searchByStorePlaceAndSupplierId(storePlace, supplierId, type);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory) && StringUtils.isBlank(storePlace) && StringUtils.isNotBlank(supplierId)) {
            // 只有供應商名
            itemIdAmountInventoryMap = inventoryService.searchBySupplierId(supplierId, type);
        } else {
            // 全空白搜尋全部
            itemIdAmountInventoryMap = inventoryService.searchByName("", type);
        }

        final Map<String, Long> receiveItemIdInventoryAmountMap = itemIdAmountInventoryMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().values().stream().flatMap(List::stream).count()
                ));
        final List<Inventory> inventoryList = itemIdAmountInventoryMap.values().stream()
                .flatMap(innerMap -> innerMap.values().stream())
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        Inventory::getReceiveItemId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .toList();
        final List<String> productIds = inventoryList.stream().map(Inventory::getProductId).toList();
        final Map<String, Product> tempProductMap = productService.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<InventoryDTO> finalInventory = inventoryList.stream()
                .map(inventory -> {
                    final Long totalAmount = receiveItemIdInventoryAmountMap.get(inventory.getReceiveItemId());
                    final Product product = tempProductMap.get(inventory.getProductId());
                    return constructInventoryDTO(inventory, product, String.valueOf(totalAmount));
                })
                .sorted(Comparator.comparing(InventoryDTO::getExpiryDate))
                .toList();

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(finalInventory);
        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/getTodaySize")
    public ResponseEntity<?> getTodaySize(HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[getTodaySize]");
        }
        ResultStatus resultStatus = new ResultStatus();

        //今日放進冰箱的
        final int size = inventoryService.findByStoreDateIsTodayAndNoUsedAndNoDeleteAndInValidPeriod().size();

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(size);
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/exportExcel/{type}")
    public void exportExcel (@PathVariable String type, @RequestBody List<InventoryDTO> inventoryDTOList, HttpSession session, HttpServletResponse response) throws Exception {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[exportExcel]");
        }
        FileUtils.constructExcel(inventoryDTOList, type, response, member);
    }


    private InventoryDTO constructInventoryDTO(Inventory inventory, Product product, String totalAmount) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setName(product.getName());
        dto.setSerialNumber(inventory.getSerialNumber());
        dto.setReceiveFormId(inventory.getReceiveFormId());
        dto.setReceiveFormNumber(inventory.getReceiveFormNumber());
        dto.setReceiveItemId(inventory.getReceiveItemId());
        dto.setUsedDate(inventory.getUsedDate());
        dto.setStoreDate(inventory.getStoreDate());
        dto.setStoreNumber(inventory.getStoreNumber());
        dto.setExpiryDate(inventory.getExpiryDate());
        dto.setStorePlace(inventory.getStorePlace());
        dto.setStorePlaceLabel(inventory.getStorePlace() != null ? inventory.getStorePlace().getLabel() : null);
        dto.setPackageForm(inventory.getPackageForm());
        dto.setPackageFormLabel(inventory.getPackageForm() != null ? inventory.getPackageForm().getLabel() : null);
        dto.setPackageUnit(inventory.getPackageUnit());
        dto.setPackageUnitLabel(inventory.getPackageUnit() != null ? inventory.getPackageUnit().getLabel() : null);
        dto.setPackageQuantity(inventory.getPackageQuantity());
        dto.setPackageNumber(inventory.getPackageNumber());
        dto.setMainCategory(product.getMainCategory());
        dto.setSubCategory(product.getSubCategory());
        dto.setMainCategoryLabel(product.getMainCategory() != null ? product.getMainCategory().getLabel() : null);
        dto.setSubCategoryLabel(product.getSubCategory() != null ? product.getSubCategory().getLabel() : null);
        dto.setOverdueNotice(inventory.getOverdueNotice());
        dto.setSerialNumber(product.getSerialNumber());
        dto.setExpiryTime(genDateFormatted(sdf.format(new Date()), inventory.getExpiryDate()));
        dto.setExistedTime(genDateFormatted(inventory.getStoreDate(), sdf.format(new Date())));
        dto.setNoticeDate(genNoticeDateFormatted(inventory.getExpiryDate(), inventory.getOverdueNotice()));
        dto.setTotalAmount(totalAmount);
        dto.setCoverPath(configProperties.getPicShowPath() + product.getCoverName());
        dto.setCreationDate(inventory.getCreationDate());
        dto.setModificationDate(inventory.getModificationDate());
        dto.setDeletionDate(inventory.getDeletionDate());
        dto.setCreator(inventory.getCreator());
        dto.setModifier(inventory.getModifier());
        dto.setProductId(inventory.getProductId());
        return dto;
    }
}
