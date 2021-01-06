package com.aaron.controller;

import com.aaron.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author Aaron
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2021/1/5
 */
@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/parse/{keywords}")
    public boolean parseSearch(@PathVariable("keywords") String keywords) throws IOException {
        return goodsService.addGoodsIndex(keywords);
    }

    @GetMapping("/search/{keywords}/{pageNum}/{pageSize}")
    public List<Map<String,Object>> searchDatas(@PathVariable("keywords") String Keywords,
                                                @PathVariable("pageNum") int pageNum,
                                                @PathVariable("pageSize") int pageSize) throws IOException {
        return goodsService.queryGoods(Keywords, pageNum, pageSize);

    }

    @GetMapping("/highsearch/{keywords}/{pageNum}/{pageSize}")
    public List<Map<String,Object>> highLightSearch(@PathVariable("keywords") String Keywords,
                                                @PathVariable("pageNum") int pageNum,
                                                @PathVariable("pageSize") int pageSize) throws IOException {
        return goodsService.highLightGoods(Keywords, pageNum, pageSize);

    }
}
