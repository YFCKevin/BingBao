package com.yfckevin.bingBao.controller;

import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.dto.MemberDTO;
import com.yfckevin.bingBao.dto.SearchDTO;
import com.yfckevin.bingBao.dto.SupplierDTO;
import com.yfckevin.bingBao.entity.Supplier;
import com.yfckevin.bingBao.exception.ResultStatus;
import com.yfckevin.bingBao.service.SupplierService;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class SupplierController {
    Logger logger = LoggerFactory.getLogger(SupplierController.class);
    private final SimpleDateFormat sdf;
    private final ConfigProperties configProperties;
    private final SupplierService supplierService;

    public SupplierController(@Qualifier("sdf") SimpleDateFormat sdf, ConfigProperties configProperties, SupplierService supplierService) {
        this.sdf = sdf;
        this.configProperties = configProperties;
        this.supplierService = supplierService;
    }

    /**
     * 新增或更新供應商
     * @param dto
     * @param session
     * @return
     */
    @PostMapping("/saveSupplier")
    public ResponseEntity<?> saveSupplier (@RequestBody SupplierDTO dto, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[saveSupplier]");
        }
        ResultStatus resultStatus = new ResultStatus();

        if (StringUtils.isBlank(dto.getId())) { //新增
            Supplier supplier = new Supplier();
            supplier.setAddress(dto.getAddress());
            supplier.setName(dto.getName());
            supplier.setPhone(dto.getPhone());
            supplier.setWebsite(dto.getWebsite());
            supplier.setCreator(member.getName());
            supplier.setCreationDate(sdf.format(new Date()));
            Supplier savedSupplier = supplierService.save(supplier);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(savedSupplier);
        } else {    //更新
            supplierService.findById(dto.getId())
                    .map(supplier -> {
                        supplier.setAddress(dto.getAddress());
                        supplier.setName(dto.getName());
                        supplier.setPhone(dto.getPhone());
                        supplier.setWebsite(dto.getWebsite());
                        supplier.setModifier(member.getName());
                        supplier.setModificationDate(sdf.format(new Date()));
                        Supplier savedSupplier = supplierService.save(supplier);
                        resultStatus.setCode("C000");
                        resultStatus.setMessage("成功");
                        resultStatus.setData(savedSupplier);
                        return resultStatus;
                    })
                    .orElseGet(() -> {
                        resultStatus.setCode("C002");
                        resultStatus.setMessage("查無供應商");
                        return resultStatus;
                    });
        }
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 我的供應商列表
     * @param session
     * @return
     */
    @GetMapping("/allSuppliers")
    public ResponseEntity<?> allSuppliers (HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[allSuppliers]");
        }
        ResultStatus resultStatus = new ResultStatus();

        List<Supplier> supplierList = supplierService.findAllByOrderByCreationDateDesc();
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(supplierList);
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 查詢單一供應商資訊
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/supplierInfo/{id}")
    public ResponseEntity<?> supplierInfo (@PathVariable String id, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[supplierInfo]");
        }
        ResultStatus resultStatus = new ResultStatus();

        supplierService.findById(id)
                .map(supplier -> {
                    resultStatus.setCode("C000");
                    resultStatus.setMessage("成功");
                    resultStatus.setData(supplier);
                    return resultStatus;
                })
                .orElseGet(() -> {
                    resultStatus.setCode("C002");
                    resultStatus.setMessage("查無供應商");
                    return resultStatus;
                });
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 刪除供應商
     * @param id
     * @param session
     * @return
     */
    @DeleteMapping("/deleteSupplier/{id}")
    public ResponseEntity<?> deleteSupplier (@PathVariable String id, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[deleteSupplier]");
        }
        ResultStatus resultStatus = new ResultStatus();

        supplierService.findById(id)
                .map(supplier -> {
                    supplier.setDeletionDate(sdf.format(new Date()));
                    final Supplier savedSupplier = supplierService.save(supplier);
                    resultStatus.setCode("C000");
                    resultStatus.setMessage("成功");
                    resultStatus.setData(savedSupplier);
                    return resultStatus;
                })
                .orElseGet(() -> {
                    resultStatus.setCode("C002");
                    resultStatus.setMessage("查無供應商");
                    return resultStatus;
                });
        return ResponseEntity.ok(resultStatus);
    }


    /**
     * 模糊查詢供應商資料
     * @param searchDTO
     * @param session
     * @return
     */
    @PostMapping("/supplierSearch")
    public ResponseEntity<?> supplierSearch (@RequestBody SearchDTO searchDTO, HttpSession session){

        final MemberDTO member = (MemberDTO) session.getAttribute("member");
        if (member != null) {
            logger.info("[" + member.getName() + "]" + "[supplierSearch]");
        }
        ResultStatus resultStatus = new ResultStatus();

        List<Supplier> supplierList = supplierService.findSupplierByCondition(searchDTO.getKeyword());
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(supplierList);

        return ResponseEntity.ok(resultStatus);
    }
}
