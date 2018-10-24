package com.atguigu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manager.BaseAttrInfo;
import com.atguigu.gmall.manager.SkuEsService;
import com.atguigu.gmall.manager.SkuService;
import com.atguigu.gmall.manager.es.SkuBaseAttrEsVo;
import com.atguigu.gmall.manager.es.SkuInfoEsVo;
import com.atguigu.gmall.manager.es.SkuSearchParamEsVo;
import com.atguigu.gmall.manager.es.SkuSearchResultEsVo;
import com.atguigu.gmall.manager.sku.SkuInfo;
import com.atguigu.gmall.search.constant.EsConstant;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SkuEsServiceImpl implements SkuEsService {

   @Reference
    SkuService skuService;


   @Autowired
    JestClient jestClient;

    /**
     * 这是一个异步的上架方法
     * @param skuId
     */
    @Async //异步，表示这是一个异步调用
    @Override
    public void onSale(Integer skuId) {
        //查出这个sku对应的详细信息
        SkuInfo info = skuService.getSkuInfoBySkuId(skuId);
        System.out.println("mmmmmmmmmmmmmmm"+info);
        log.info("获取到的sku信息：{}",info);

        SkuInfoEsVo skuInfoEsVo = new SkuInfoEsVo();
        //将查询到的信息拷贝出来
        BeanUtils.copyProperties(info,skuInfoEsVo);
        //查出当前sku的所有平台属性值
        List<SkuBaseAttrEsVo> vos= skuService.getSkuBaseAttrValueIds(skuId);

        skuInfoEsVo.setBaseAttrEsVos(vos);

        //保存信息到es
        Index index = new Index.Builder(skuInfoEsVo).index(EsConstant.GMALL_INDEX).type(EsConstant.GMALL_SKU_TYPE)
                .id(skuInfoEsVo.getId() + "").build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            log.error("es数据保存出问题了，{}",e);
        }
    }

    /**
     * 安装查询参数查到页面所需要的数据
     * @param paramEsVo
     * @return
     */
    @Override
    public SkuSearchResultEsVo searchSkuFromES(SkuSearchParamEsVo paramEsVo) {
        SkuSearchResultEsVo resultEsVo=null;
        //0.DSL的大拼串
        String queryDsl=buildSkuSearchQueryDSL(paramEsVo);

        //1.传入dsl语句
        Search search = new Search.Builder(queryDsl).addIndex(EsConstant.GMALL_INDEX).addType(EsConstant.GMALL_SKU_TYPE).build();
        //2.执行查询
        try {
            SearchResult result = jestClient.execute(search);
            //把查询出来的result封装成能给页面返回的SkuSearchResultEsVo对象
            resultEsVo=buildSkuSearchResult(result);
            resultEsVo.setPageNo(paramEsVo.getPageNo());
            return  resultEsVo;
        } catch (IOException e) {
            log.error("ES查询出故障：{}",e);
        }
        return resultEsVo;
    }

    @Async
    @Override
    public void updateHotScore(Integer skuId, Long hincrBy) {
        String updateHotScore ="{\"doc\":{\"hotScore\":"+hincrBy+"}}";
        Update update = new Update.Builder(updateHotScore).index(EsConstant.GMALL_INDEX).type(EsConstant.GMALL_SKU_TYPE)
                .id(skuId + "").build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            log.error("es更新热度出问题了",e);
        }
    }

    //将查出的结果构建为页面能用的vo对象数据
    private  SkuSearchResultEsVo buildSkuSearchResult(SearchResult result){
        SkuSearchResultEsVo resultEsVo = new SkuSearchResultEsVo();

        //所有skuInfo 的集合

        List<SkuInfoEsVo> skuInfoEsVoList=null;
        //1.从es的搜索结果中找到所有skuInfo的信息
        //拿到命中的所有记录
        List<SearchResult.Hit<SkuInfoEsVo, Void>> hits = result.getHits(SkuInfoEsVo.class);
        if(hits == null||hits.size() == 0){
            return  null;
        }else {
            //查到了数据
            skuInfoEsVoList=new ArrayList<>(hits.size());
            //遍历所有的命中记录，取出每一个skuInfo放到list中，并且设置好高亮
            for (SearchResult.Hit<SkuInfoEsVo, Void> hit : hits) {
                SkuInfoEsVo source = hit.source;

                //有可能有高亮的
                Map<String, List<String>> highlight = hit.highlight;
                //普通的fei非全文模糊匹配的是没有高亮的

                if(highlight!=null){
                    String highText = highlight.get("skuName").get(0);
                    //替换为高亮
                    source.setSkuName(highText);
                }
                skuInfoEsVoList.add(source);
            }
        }
        //保存了skuInfo信息
        resultEsVo.setSkuInfoEsVos(skuInfoEsVoList);
        //总记录数
        resultEsVo.setTotal(result.getTotal().intValue());


        //从聚合的数据中取出所有平台属性以及他的值
        List<BaseAttrInfo> baseAttrInfos=getBaseAttrInfoGroupByValueId(result);
        resultEsVo.setBaseAttrInfos(baseAttrInfos);

        return  resultEsVo;
    }


    /**
     * 根据es中查询到的聚合的结果找到所有涉及到的平台属性对应的值
     * @param result
     * @return
     */
    private List<BaseAttrInfo> getBaseAttrInfoGroupByValueId(SearchResult result){
        MetricAggregation aggregations = result.getAggregations();
        //获取term聚合出来的数据
        TermsAggregation valueIdAggs = aggregations.getTermsAggregation("valueIdAggs");
        List<TermsAggregation.Entry> buckets = valueIdAggs.getBuckets();
        List<Integer> valueIds=new ArrayList<>();
        //遍历所有buckets
        for (TermsAggregation.Entry bucket : buckets) {
            String key = bucket.getKey();
            valueIds.add(Integer.parseInt(key));
        }
        //查询所有涉及到的平台属性以及值
        return  skuService.getSkuBaseAttrInfoGroupByValueId(valueIds);

    }







    //构造QueryDsl字符串
    private String buildSkuSearchQueryDSL(SkuSearchParamEsVo paramEsVo){
        //1.创建出一个能搜索数据的构建器 ，构建出DSL
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //过滤三级分类信息
        if(paramEsVo.getCatalog3Id()!=null){
            TermQueryBuilder termCatalog3Id = new TermQueryBuilder("catalog3Id", paramEsVo.getCatalog3Id());
            boolQuery.filter(termCatalog3Id);
        }
        //过滤valueId的信息
        if(paramEsVo.getValueId()!=null && paramEsVo.getValueId().length>0){
            //过滤页面提交来的所有valueId
            for (Integer vid : paramEsVo.getValueId()) {
                TermQueryBuilder termValueId = new TermQueryBuilder("baseAttrEsVos.valueId", vid);
                boolQuery.filter(termValueId);
            }
        }

        //搜索
        if(!StringUtils.isEmpty(paramEsVo.getKeyword())){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", paramEsVo.getKeyword());
            boolQuery.must(matchQueryBuilder);
            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            sourceBuilder.highlight(highlightBuilder);
        }

        //以上查询和过滤完成
        sourceBuilder.query(boolQuery);

        //排序
        if(!StringUtils.isEmpty(paramEsVo.getSortField())){
            SortOrder sortOrder=null;
            switch (paramEsVo.getSortOrder()){
                case "desc" :sortOrder=SortOrder.DESC;break;
                case "asc":sortOrder=SortOrder.ASC;break;
                default:sortOrder=SortOrder.DESC;
            }
            sourceBuilder.sort(paramEsVo.getSortField(),sortOrder);
        }

        //分页，页面传入的是页面，计算下一个从第几个开始查
        sourceBuilder.from((paramEsVo.getPageNo()-1)*paramEsVo.getPageSize());
        sourceBuilder.size(paramEsVo.getPageSize());

        //聚合
        TermsBuilder termsBuilder = new TermsBuilder("valueIdAggs");
        termsBuilder.field("baseAttrEsVos.valueId");
        sourceBuilder.aggregation(termsBuilder);

        String dsl=sourceBuilder.toString();

        return dsl;
    }



}
