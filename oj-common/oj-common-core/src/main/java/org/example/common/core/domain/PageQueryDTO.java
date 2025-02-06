package org.example.common.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageQueryDTO {

    private Integer pageSize = 10;  //每页的数据  需要传给前端，设置默认值是10

    private Integer pageNum = 1;   //页数   需要传给前端，设置默认值是1

    //上述两条数据必须传输是因为在搜索的条件为空时，可以显示原本的数据信息，因此必须传输页数和每页的数据
}
