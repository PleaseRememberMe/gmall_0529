package com.atguigu.gmall.manager;

import java.util.List;
import com.atguigu.gmall.manager.spu.SpuInfo;

public interface SpuInfoService {

    List<SpuInfo> getSpuInfoByC3Id(Integer catalog3Id);
}
