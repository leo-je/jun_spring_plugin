package diboot.core.test.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diboot.core.config.BaseConfig;
import com.diboot.core.entity.Dictionary;
import com.diboot.core.service.impl.DictionaryServiceImpl;
import com.diboot.core.util.BeanUtils;
import com.diboot.core.util.V;
import com.diboot.core.vo.KeyValue;
import com.diboot.core.vo.Pagination;
import com.diboot.core.vo.PagingJsonResult;
import diboot.core.test.StartupApplication;
import diboot.core.test.config.SpringMvcConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *  BaseService接口实现测试 (需先执行example中的初始化SQL)
 * @author Wujun
 * @version v2.0
 * @date 2019/06/15
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpringMvcConfig.class})
@SpringBootTest(classes = {StartupApplication.class})
public class BaseServiceTest {

    @Autowired
    @Qualifier("dictionaryService")
    DictionaryServiceImpl dictionaryService;

    @Test
    public void testGet(){
        // 查询总数
        int count = dictionaryService.getEntityListCount(null);
        Assert.assertTrue(count > 0);
        // 查询list
        List<Dictionary> dictionaryList = dictionaryService.getEntityList(null);
        Assert.assertTrue(V.notEmpty(dictionaryList));
        Assert.assertTrue(dictionaryList.size() == count);
        // 第一页数据
        List<Dictionary> pageList = dictionaryService.getEntityList(null, new Pagination());
        Assert.assertTrue(pageList.size() > 0 && pageList.size() <= BaseConfig.getPageSize());

        // 查询单个记录
        Long id = dictionaryList.get(0).getId();
        Dictionary first = dictionaryService.getEntity(id);
        Assert.assertTrue(first != null);

        // 只查询第一条记录对应type类型的
        LambdaQueryWrapper<Dictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dictionary::getType, first.getType());
        dictionaryList = dictionaryService.getEntityList(queryWrapper);
        Assert.assertTrue(V.notEmpty(dictionaryList));
        // 结果type值一致
        dictionaryList.stream().forEach(m -> {
            Assert.assertTrue(m.getType().equals(first.getType()));
        });

        // 根据id集合去批量查询
        List<Long> ids = BeanUtils.collectIdToList(dictionaryList);
        dictionaryList = dictionaryService.getEntityListByIds(ids);
        Assert.assertTrue(V.notEmpty(dictionaryList));
    }

    @Test
    @Transactional
    public void testCreateUpdateAndDelete(){
        // 创建
        String TYPE = "ID_TYPE";
        Dictionary dictionary = new Dictionary();
        dictionary.setType(TYPE);
        dictionary.setItemName("证件类型");
        dictionary.setParentId(0L);
        dictionaryService.createEntity(dictionary);

        // 查询是否创建成功
        LambdaQueryWrapper<Dictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dictionary::getType, TYPE);
        List<Dictionary> dictionaryList = dictionaryService.getEntityList(queryWrapper);
        Assert.assertTrue(V.notEmpty(dictionaryList));

        // 更新
        dictionary.setItemName("证件类型定义");
        dictionaryService.updateEntity(dictionary);
        Dictionary dictionary2 = dictionaryService.getEntity(dictionary.getId());
        Assert.assertTrue(dictionary2.getItemName().equals(dictionary.getItemName()));
    }

    @Test
    @Transactional
    public void testBatchCreate(){
        // 创建
        String TYPE = "ID_TYPE";
        // 定义
        Dictionary dictionary = new Dictionary();
        dictionary.setType(TYPE);
        dictionary.setItemName("证件类型");
        dictionary.setParentId(0L);
        boolean success = dictionaryService.createEntity(dictionary);
        Assert.assertTrue(success);

        // 子项
        List<Dictionary> dictionaryList = new ArrayList<>();
        String[] itemNames = {"身份证", "驾照", "护照"}, itemValues = {"SFZ","JZ","HZ"};
        for(int i=0; i<itemNames.length; i++){
            Dictionary dict = new Dictionary();
            dict.setType(TYPE);
            dict.setItemName(itemNames[i]);
            dict.setItemValue(itemValues[i]);
            dict.setParentId(dictionary.getId());
            dictionaryList.add(dict);
        }
        success = dictionaryService.createEntities(dictionaryList);
        Assert.assertTrue(success);

        dictionaryList.get(2).setCreateTime(new Date());
        dictionaryList.get(2).setItemValue("HZ2");
        dictionaryService.updateEntity(dictionaryList.get(2));
        Assert.assertTrue(success);
    }

    @Test
    public void testKV(){
        List<KeyValue> keyValues = dictionaryService.getKeyValueList("GENDER");
        Assert.assertTrue(keyValues.size() == 2);
        Assert.assertTrue(keyValues.get(0).getV().equals("M"));
        Map<String, Object> kvMap = BeanUtils.convertKeyValueList2Map(keyValues);
        Assert.assertTrue(kvMap.get("女").equals("F"));
    }

    @Test
    public void testIpage2PagingJsonResult(){
        // 查询是否创建成功
        LambdaQueryWrapper<Dictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dictionary::getType, "GENDER");
        queryWrapper.orderByDesc(Dictionary::getId);
        IPage<Dictionary> dp = new Page<>();
        ((Page<Dictionary>) dp).addOrder(OrderItem.desc("id"));

        IPage<Dictionary> dictionaryPage = dictionaryService.page(dp, queryWrapper);

        PagingJsonResult pagingJsonResult = new PagingJsonResult(dictionaryPage);
        Assert.assertTrue(V.notEmpty(pagingJsonResult));
        Assert.assertTrue(pagingJsonResult.getPage().getOrderBy().equals("id:DESC"));
    }

}