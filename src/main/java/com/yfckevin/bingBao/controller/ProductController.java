package com.yfckevin.bingBao.controller;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;
import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.*;
import com.yfckevin.bingBao.entity.Product;
import com.yfckevin.bingBao.entity.ReceiveItem;
import com.yfckevin.bingBao.entity.TempDetail;
import com.yfckevin.bingBao.entity.TempMaster;
import com.yfckevin.bingBao.enums.PackageForm;
import com.yfckevin.bingBao.exception.ResultStatus;
import com.yfckevin.bingBao.service.*;
import com.yfckevin.bingBao.utils.FileUtils;
import com.yfckevin.bingBao.utils.PNUtil;
import com.yfckevin.bingBao.utils.SNUtil;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ProductController {
    Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final SimpleDateFormat sdf;
    private final SimpleDateFormat picSuffix;
    private final ProductService productService;
    private final GoogleVisionService googleVisionService;
    private final TempMasterService tempMasterService;
    private final ReceiveItemService receiveItemService;
    private final OpenAiService openAiService;
    private final ConfigProperties configProperties;

    public ProductController(@Qualifier("sdf") SimpleDateFormat sdf, @Qualifier("picSuffix") SimpleDateFormat picSuffix, ProductService productService, GoogleVisionService googleVisionService, TempMasterService tempMasterService, ReceiveItemService receiveItemService, OpenAiService openAiService, ConfigProperties configProperties) {
        this.sdf = sdf;
        this.picSuffix = picSuffix;
        this.productService = productService;
        this.googleVisionService = googleVisionService;
        this.tempMasterService = tempMasterService;
        this.receiveItemService = receiveItemService;
        this.openAiService = openAiService;
        this.configProperties = configProperties;
    }


    /**
     * 手寫輸入食材資訊
     *
     * @param dto
     * @param session
     * @return
     */
    @PostMapping("/saveProduct")
    public ResponseEntity<?> saveProduct(@ModelAttribute ProductDTO dto, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[saveProduct]");
        }
        ResultStatus resultStatus = new ResultStatus();

        Product product = new Product();

        final MultipartFile nameFile = dto.getMultipartFile();

        if (nameFile != null && !nameFile.isEmpty() && nameFile.getSize() != 0) {
            final String extension = FilenameUtils.getExtension(nameFile.getOriginalFilename());
            String fileName = picSuffix.format(new Date()) + "." + extension;
            System.out.println("fileName: " + fileName);

            try {
                System.out.println("上傳圖片");
                FileUtils.saveUploadedFile(nameFile, configProperties.getPicSavePath() + fileName);
            } catch (IOException e) {
                System.out.println("圖片上傳失敗");
                logger.error(e.getMessage(), e);
                resultStatus.setCode("C009");
                resultStatus.setMessage("圖片上傳失敗");
                return ResponseEntity.ok(resultStatus);
            }
            product.setCoverName(fileName);
        }

//            product.setCreator(member.getName());
        product.setName(dto.getName());
        product.setCreationDate(sdf.format(new Date()));
        product.setPackageForm(dto.getPackageForm());
        product.setDescription(dto.getDescription());
        product.setOverdueNotice(dto.getOverdueNotice());
        product.setMainCategory(dto.getMainCategory());
        if (PackageForm.COMPLETE.equals(dto.getPackageForm())) {
            product.setPackageQuantity(dto.getPackageQuantity());
        }
        product.setPackageUnit(dto.getPackageUnit());
        product.setPrice(dto.getPrice());
        product.setSerialNumber(SNUtil.generateSerialNumber());
        Product savedProduct = productService.save(product);

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(savedProduct);
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/editProduct")
    public ResponseEntity<?> editProduct(@ModelAttribute ProductDTO dto, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[editProduct]");
        }
        ResultStatus resultStatus = new ResultStatus();

        System.out.println(dto);

        Optional<Product> productOpt = productService.findById(dto.getId());
        if (productOpt.isEmpty()) {
            resultStatus.setCode("C001");
            resultStatus.setMessage("查無食材");
            return ResponseEntity.ok(resultStatus);
        } else {
            Product product = productOpt.get();
            product.setName(dto.getName());
            product.setPrice(dto.getPrice());
            product.setPackageUnit(dto.getPackageUnit());
            product.setSerialNumber(dto.getSerialNumber());
            product.setPackageQuantity(dto.getPackageQuantity());
            product.setOverdueNotice(dto.getOverdueNotice());
            product.setMainCategory(dto.getMainCategory());
            product.setSubCategory(dto.getSubCategory());
            product.setModificationDate(sdf.format(new Date()));
//            product.setModifier(member.getName());
            product.setDescription(dto.getDescription());
            product.setPackageForm(dto.getPackageForm());

            final MultipartFile nameFile = dto.getMultipartFile();
            if (nameFile != null && !nameFile.isEmpty() && nameFile.getSize() != 0) {   //更新圖片
                final String extension = FilenameUtils.getExtension(nameFile.getOriginalFilename());
                String fileName = picSuffix.format(new Date()) + "." + extension;
                System.out.println("fileName: " + fileName);

                try {
                    System.out.println("上傳圖片");
                    FileUtils.saveUploadedFile(nameFile, configProperties.getPicSavePath() + fileName);

                    //刪除舊圖檔
                    if (StringUtils.isNotBlank(product.getCoverName())) {
                        FileUtils.delete(Paths.get(configProperties.getPicSavePath() + product.getCoverName()));
                    }
                } catch (IOException e) {
                    System.out.println("圖片上傳失敗");
                    logger.error(e.getMessage(), e);
                    resultStatus.setCode("C009");
                    resultStatus.setMessage("圖片上傳失敗");
                    return ResponseEntity.ok(resultStatus);
                }
                product.setCoverName(fileName);
            }
            final Product savedProduct = productService.save(product);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(savedProduct);
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 拍照匯入食材資訊
     *
     * @param imageDTO
     * @param session
     * @return
     */
    @PostMapping("/convertDataByPhoto")
    public ResponseEntity<?> importDataByPhoto(@RequestBody ImageRequestDTO imageDTO, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[convertDataByPhoto]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final byte[] decodeImg = Base64.getDecoder().decode(imageDTO.getImage());

        String fileName = "";
        try {
            //儲存照片
            fileName = picSuffix.format(new Date()) + ".jpg";
            System.out.println("fileName: " + fileName);
            String filePath = configProperties.getAiPicSavePath() + fileName;
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(decodeImg);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ByteString imgBytes = ByteString.copyFrom(decodeImg);
        Image img = Image.newBuilder().setContent(imgBytes).build();

        Feature feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(img)
                .build();

        List<AnnotateImageRequest> requests = new ArrayList<>();
        requests.add(request);

        try {
            String rawText = googleVisionService.extractText(requests);
            rawText = rawText.replaceAll("\\r?\\n", "");
            logger.info("圖轉文：{}", rawText);

            final TempMaster tempMaster = openAiService.completion(rawText);
            if (tempMaster != null) {
                tempMaster.setCreationDate(sdf.format(new Date()));
                tempMaster.setCoverName(fileName);
//                tempMaster.setCreator(member.getName());
                final TempMaster savedTempMaster = tempMasterService.save(tempMaster);
                resultStatus.setCode("C000");
                resultStatus.setMessage("成功");
                resultStatus.setData(savedTempMaster);
            } else {
                resultStatus.setCode("C010");
                resultStatus.setMessage("轉換失敗");
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultStatus.setCode("C999");
            resultStatus.setMessage(e.getMessage());
        }
        return ResponseEntity.ok(resultStatus);
    }


    @PostMapping("/importIntoDB")
    public ResponseEntity<?> importIntoDB(@ModelAttribute ProductDTOListWrapperDTO productDTOListWrapperDTO, HttpSession session) {
        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[importIntoDB]");
        }
        ResultStatus resultStatus = new ResultStatus();

        System.out.println(productDTOListWrapperDTO);

        List<Product> productList = new ArrayList<>();
        final String packageNumber = PNUtil.generatePackageNumber();

        for (ProductDTO dto : productDTOListWrapperDTO.getProductDTOList()) {

            Product product = new Product();

            final MultipartFile nameFile = dto.getMultipartFile();

            if (nameFile != null && !nameFile.isEmpty() && nameFile.getSize() != 0) {
                final String extension = FilenameUtils.getExtension(nameFile.getOriginalFilename());
                String fileName = String.format("%d", System.currentTimeMillis()) + "." + extension;
                System.out.println("fileName: " + fileName);

                try {
                    System.out.println("上傳圖片");
                    FileUtils.saveUploadedFile(nameFile, configProperties.getPicSavePath() + fileName);
                } catch (IOException e) {
                    System.out.println("圖片上傳失敗");
                    logger.error(e.getMessage(), e);
                    resultStatus.setCode("C009");
                    resultStatus.setMessage("圖片上傳失敗");
                    return ResponseEntity.ok(resultStatus);
                }
                product.setCoverName(fileName);
            }

//            product.setCreator(member.getName());
            product.setName(dto.getName());
            product.setCreationDate(sdf.format(new Date()));
            product.setPackageForm(dto.getPackageForm());
            product.setDescription(dto.getDescription());
            product.setOverdueNotice(dto.getOverdueNotice());
            product.setMainCategory(dto.getMainCategory());
            product.setSubCategory(dto.getSubCategory());
            if (PackageForm.COMPLETE.equals(dto.getPackageForm())) {
                product.setPackageQuantity(dto.getPackageQuantity());
                product.setPackageUnit(dto.getPackageUnit());
            }
            product.setPrice(dto.getPrice());
            product.setSerialNumber(SNUtil.generateSerialNumber());
            product.setPackageNumber(packageNumber);
            productList.add(product);
        }
        List<Product> savedProductList = productService.saveAll(productList);
        List<ProductDTO> productDTOS = savedProductList.stream().map(this::constructProductDTO).toList();

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(productDTOS);
        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/allTempMasters")
    public ResponseEntity<?> allTempMasters(HttpSession session) {
        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[allTempMasters]");
        }
        ResultStatus resultStatus = new ResultStatus();

        List<TempMaster> tempMasterList = tempMasterService.findAllByOrderByCreationDateDesc();
        final List<TempMasterDTO> tempMasterDTOList = tempMasterList.stream().map(this::constructTempMasterDTO).toList();

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(tempMasterDTOList);
        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/getTempMasterInfo/{id}")
    public ResponseEntity<?> getTempMasterInfo(@PathVariable String id, HttpSession session) {
        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[getTempMasterInfo]");
        }
        ResultStatus resultStatus = new ResultStatus();

        return tempMasterService.findById(id)
                .map(t -> {
                    final List<TempDetailDTO> detailDTOList = t.getTempDetails().stream()
                            .map(ProductController::constructTempDetailDTO).toList();
                    resultStatus.setCode("C000");
                    resultStatus.setMessage("成功");
                    resultStatus.setData(detailDTOList);
                    return ResponseEntity.ok(resultStatus);
                })
                .orElseGet(() -> {
                    resultStatus.setCode("C005");
                    resultStatus.setMessage("查無匯入資料");
                    return ResponseEntity.ok(resultStatus);
                });
    }


    /**
     * 我的食材清單
     *
     * @param session
     * @return
     */
    @GetMapping("/myProducts")
    public ResponseEntity<?> myProducts(HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[myProducts]");
        }
        ResultStatus resultStatus = new ResultStatus();

        List<Product> productList = productService.findAllByDeletionDateIsNullOrderByCreationDateDesc();
        List<ProductDTO> productDTOList = new ArrayList<>();
        for (Product product : productList) {
            ProductDTO dto = constructProductDTO(product);
            productDTOList.add(dto);
        }

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(productDTOList);
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 刪除食材
     *
     * @param id
     * @param session
     * @return
     */
    @DeleteMapping("/deleteProduct/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[deleteProduct]");
        }
        ResultStatus resultStatus = new ResultStatus();

        return productService.findById(id)
                .map(p -> {
                    Optional<ReceiveItem> opt = receiveItemService.findFirstByProductId(id);
                    if (opt.isEmpty()) {
                        try {
                            FileUtils.delete(Paths.get(configProperties.getPicSavePath() + p.getCoverName()));
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                        productService.deleteById(id);
                        resultStatus.setCode("C000");
                        resultStatus.setMessage("成功");
                    } else {
                        resultStatus.setCode("C011");
                        resultStatus.setMessage("該模板已使用");
                    }
                    return ResponseEntity.ok(resultStatus);
                })
                .orElseGet(() -> {
                    resultStatus.setCode("C001");
                    resultStatus.setMessage("查無食材");
                    return ResponseEntity.ok(resultStatus);
                });
    }


    /**
     * 查詢單一食材資訊
     *
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/productInfo/{id}")
    public ResponseEntity<?> productInfo(@PathVariable String id, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[productInfo]");
        }
        ResultStatus resultStatus = new ResultStatus();

        return productService.findById(id)
                .map(p -> {
                    resultStatus.setCode("C000");
                    resultStatus.setMessage("成功");
                    resultStatus.setData(constructProductDTO(p));
                    return ResponseEntity.ok(resultStatus);
                })
                .orElseGet(() -> {
                    resultStatus.setCode("C001");
                    resultStatus.setMessage("查無食材");
                    return ResponseEntity.ok(resultStatus);
                });
    }


    /**
     * 模糊查詢食材資訊
     *
     * @param searchDTO
     * @param session
     * @return
     */
    @PostMapping("productSearch")
    public ResponseEntity<?> productSearch(@RequestBody SearchDTO searchDTO, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[productSearch]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final String keyword = searchDTO.getKeyword().trim();
        final String mainCategory = searchDTO.getMainCategory();
        final String subCategory = searchDTO.getSubCategory();
        System.out.println(keyword + " / " + mainCategory + " / " + subCategory);
        List<Product> productList = new ArrayList<>();
        if (StringUtils.isNotBlank(keyword) && StringUtils.isBlank(mainCategory) && StringUtils.isBlank(subCategory)) {
            // 只有輸入名稱
            productList = productService.searchProductByName(keyword);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory)) {
            // 有輸入名稱 + 只有主種類
            productList = productService.searchProductByNameAndMainCategory(keyword, mainCategory);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isBlank(subCategory)) {
            // 只有主種類
            productList = productService.searchProductByMainCategory(mainCategory);
        } else if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory)) {
            // 有輸入名稱 + 有主種類 + 有副種類
            productList = productService.searchProductByNameAndMainCategoryAndSubCategory(keyword, mainCategory, subCategory);
        } else if (StringUtils.isBlank(keyword) && StringUtils.isNotBlank(mainCategory) && StringUtils.isNotBlank(subCategory)) {
            // 有主種類 + 有副種類
            productList = productService.searchProductByMainCategoryAndSubCategory(mainCategory, subCategory);
        } else {
            // 全空白搜尋全部
            productList = productService.searchProductByName("");
        }

        final List<ProductDTO> productDTOList = productList.stream().map(this::constructProductDTO).toList();

        final Map<String, List<ProductDTO>> tempMap = productDTOList.stream()
                .collect(Collectors.groupingBy(ProductDTO::getPackageNumber));
        final Map<String, List<ProductDTO>> groupedProductMap = tempMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getValue().stream()
                                .map(ProductDTO::getName)
                                .collect(Collectors.joining("<br>")) +
                                " (" + entry.getValue().get(0).getPackageNumber() + ")",  // 加入CreationDate到key中
                        Map.Entry::getValue
                ));

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(groupedProductMap);
        return ResponseEntity.ok(resultStatus);
    }


    public ProductDTO constructProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        if (PackageForm.COMPLETE.equals(product.getPackageForm())) {
            dto.setPackageQuantity(product.getPackageQuantity());
            dto.setPackageUnit(product.getPackageUnit());
            dto.setPackageUnitLabel(product.getPackageUnit().getLabel());
        }
        dto.setSerialNumber(product.getSerialNumber());
        dto.setOverdueNotice(product.getOverdueNotice());
        dto.setMainCategory(product.getMainCategory());
        dto.setMainCategoryLabel(product.getMainCategory().getLabel());
        dto.setSubCategory(product.getSubCategory());
        dto.setSubCategoryLabel(product.getSubCategory() != null ? product.getSubCategory().getLabel() : null);
        dto.setModificationDate(product.getModificationDate());
        dto.setModifier(product.getModifier());
        dto.setDescription(product.getDescription());
        dto.setPackageForm(product.getPackageForm());
        dto.setPackageFormLabel(product.getPackageForm().getLabel());
        dto.setCreationDate(product.getCreationDate());
        dto.setCreator(product.getCreator());
        dto.setId(product.getId());
        dto.setCoverPath(configProperties.picShowPath + product.getCoverName());
        dto.setPackageNumber(product.getPackageNumber());
        return dto;
    }


    private static TempDetailDTO constructTempDetailDTO(TempDetail d) {
        TempDetailDTO dto = new TempDetailDTO();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setDescription(d.getDescription());
//        dto.setExpiryDay(d.getExpiryDay());
        dto.setMainCategoryLabel(d.getMainCategory().getLabel());
        dto.setPackageUnitLabel(d.getPackageUnit().getLabel());
        dto.setPrice(d.getPrice());
        dto.setOverdueNotice(d.getOverdueNotice());
        dto.setPackageQuantity(d.getPackageQuantity());
        dto.setPackageUnit(d.getPackageUnit());
        dto.setMainCategory(d.getMainCategory());
        dto.setTitle(d.getName());
        dto.setSubCategory(d.getSubCategory());
        if (d.getSubCategory() != null) {
            dto.setSubCategoryLabel(d.getSubCategory().getLabel());
        }
        dto.setPackageForm(PackageForm.COMPLETE);
        return dto;
    }


    private TempMasterDTO constructTempMasterDTO(TempMaster tempMaster) {
        TempMasterDTO dto = new TempMasterDTO();
        dto.setId(tempMaster.getId());
        dto.setCreator(tempMaster.getCreator());
        dto.setCreationDate(tempMaster.getCreationDate());
        dto.setModifier(tempMaster.getModifier());
        dto.setModificationDate(tempMaster.getModificationDate());
        dto.setDeletionDate(tempMaster.getDeletionDate());
        dto.setCoverPath(configProperties.getAiPicShowPath() + tempMaster.getCoverName());
        final List<TempDetail> tempDetails = tempMaster.getTempDetails();
        final List<TempDetailDTO> detailDTOList = tempDetails.stream().map(ProductController::constructTempDetailDTO).toList();
        dto.setTempDetails(detailDTOList);
        return dto;
    }
}
