package com.tanhua.domain.vo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {
    private Long counts; // 总记录数
    private Long pagesize;// 每页大小
    private Long pages;// 总页数
    private Long page;// 页码
    private List<T> items = Collections.emptyList();
    public static PageResult total(Long total,Long page, Long pagesize){
        PageResult pageResult = new PageResult();
        pageResult.setPage(page.longValue());
        pageResult.setPagesize(pagesize.longValue());
        // 总页面
        long pages = total/pagesize;
        if(total.intValue()%pagesize>0){
            pages+=1;
        }
        //pages+=total.intValue()%pagesize>0?1:0;
        pageResult.setPages(pages);
        pageResult.setCounts(total);
        return pageResult;
    }
}