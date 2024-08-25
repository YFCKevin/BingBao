package com.yfckevin.bingBao.controller;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;
import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.ImageRequestDTO;
import com.yfckevin.bingBao.dto.MemberDTO;
import com.yfckevin.bingBao.dto.ProductDTO;
import com.yfckevin.bingBao.dto.SearchDTO;
import com.yfckevin.bingBao.entity.Product;
import com.yfckevin.bingBao.entity.TempMaster;
import com.yfckevin.bingBao.exception.ResultStatus;
import com.yfckevin.bingBao.service.GoogleVisionService;
import com.yfckevin.bingBao.service.OpenAiService;
import com.yfckevin.bingBao.service.ProductService;
import com.yfckevin.bingBao.utils.FileUtils;
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
    private final OpenAiService openAiService;
    private final ConfigProperties configProperties;

    public ProductController(@Qualifier("sdf") SimpleDateFormat sdf, @Qualifier("picSuffix") SimpleDateFormat picSuffix, ProductService productService, GoogleVisionService googleVisionService, OpenAiService openAiService, ConfigProperties configProperties) {
        this.sdf = sdf;
        this.picSuffix = picSuffix;
        this.productService = productService;
        this.googleVisionService = googleVisionService;
        this.openAiService = openAiService;
        this.configProperties = configProperties;
    }


    /**
     * 手寫輸入產品資訊
     * @param dto
     * @param session
     * @return
     */
    @PostMapping("/saveProduct")
    public ResponseEntity<?> saveProduct (@ModelAttribute ProductDTO dto, HttpSession session) {

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[saveProduct]");
        }
        ResultStatus resultStatus = new ResultStatus();

        if (StringUtils.isBlank(dto.getId())) { //新增
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

            product.setCreator(member.getName());
            product.setCreationDate(sdf.format(new Date()));
            product.setPackageForm(dto.getPackageForm());
            product.setDescription(dto.getDescription());
            product.setExpiryDay(dto.getExpiryDay());
            product.setOverdueNotice(dto.getOverdueNotice());
            product.setMainCategory(dto.getMainCategory());
            product.setPackageQuantity(dto.getPackageQuantity());
            product.setPackageUnit(dto.getPackageUnit());
            product.setPrice(dto.getPrice());
            product.setSerialNumber(SNUtil.generateSerialNumber());
            product.setStorePlace(dto.getStorePlace());
            Product savedProduct = productService.save(product);

            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(savedProduct);

        } else {    //更新

            Optional<Product> productOpt = productService.findById(dto.getId());
            if (productOpt.isEmpty()) {
                resultStatus.setCode("C001");
                resultStatus.setMessage("查無產品");
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
                product.setExpiryDay(dto.getExpiryDay());
                product.setModificationDate(sdf.format(new Date()));
                product.setModifier(member.getName());
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

        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 拍照匯入產品資訊
     * @param imageDTO
     * @param session
     * @return
     */
    @PostMapping("/importDataByPhoto")
    public ResponseEntity<?> importDataByPhoto (@RequestBody ImageRequestDTO imageDTO, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[importDataByPhoto]");
        }
        ResultStatus resultStatus = new ResultStatus();

        final byte[] decodeImg = Base64.getDecoder().decode(imageDTO.getImage());
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
                resultStatus.setCode("C000");
                resultStatus.setMessage("成功");
                resultStatus.setData(tempMaster);
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


    /**
     * 我的產品清單
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

        List<Product> productList = productService.findAllByOrderByCreationDateDesc();
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
     * 刪除產品
     * @param id
     * @param session
     * @return
     */
    @DeleteMapping("/deleteProduct/{id}")
    public ResponseEntity<?> deleteProduct (@PathVariable String id, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[deleteProduct]");
        }
        ResultStatus resultStatus = new ResultStatus();

        return productService.findById(id)
                .map(p -> {
                    productService.deleteById(id);
                    resultStatus.setCode("C000");
                    resultStatus.setMessage("成功");
                    return ResponseEntity.ok(resultStatus);
                })
                .orElseGet(() -> {
                    resultStatus.setCode("C001");
                    resultStatus.setMessage("查無產品");
                    return ResponseEntity.ok(resultStatus);
                });
    }


    /**
     * 查詢單一產品資訊
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/productInfo/{id}")
    public ResponseEntity<?> productInfo (@PathVariable String id, HttpSession session){

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
                    resultStatus.setMessage("查無產品");
                    return ResponseEntity.ok(resultStatus);
                });
    }


    /**
     * 模糊查詢產品資訊
     * @param searchDTO
     * @param session
     * @return
     */
    @PostMapping("productSearch")
    public ResponseEntity<?> productSearch (@RequestBody SearchDTO searchDTO, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[productSearch]");
        }
        ResultStatus resultStatus = new ResultStatus();

        List<Product> productList = productService.searchProduct(searchDTO.getKeyword().trim());

        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(productList);
        return ResponseEntity.ok(resultStatus);
    }


    public ProductDTO constructProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setPackageUnitLabel(product.getPackageUnit().getLabel());
        dto.setSerialNumber(product.getSerialNumber());
        dto.setPackageQuantity(product.getPackageQuantity());
        dto.setOverdueNotice(product.getOverdueNotice());
        dto.setMainCategoryLabel(product.getMainCategory().getLabel());
        dto.setExpiryDay(product.getExpiryDay());
        dto.setModificationDate(product.getModificationDate());
        dto.setModifier(product.getModifier());
        dto.setDescription(product.getDescription());
        dto.setPackageFormLabel(product.getPackageForm().getLabel());
        dto.setCreationDate(product.getCreationDate());
        dto.setCreator(product.getCreator());
        dto.setCoverPath(configProperties.picShowPath + product.getCoverName());
        return dto;
    }
}
