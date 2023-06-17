package org.anyline.data.interceptor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public interface JDBCInterceptor {

    /*
     * before1
     * before2
     * before3
     * EXE
     * after1
     * after2
     * after3
     * */
    enum SWITCH {
        CONINUE("正常执行"),
        SKIP("跳过后续prepare/before/after"), //before1返回SKIP则跳过before2,before3,但不影响after2,after3,
        BREAK("中断主流程执行")
        ;
        private final String title;
        SWITCH(String title){
            this.title = title;
        }
    }
    /**
     * 执行顺序 最小的先执行
     * @return int
     */
    default int order(){
        return 10;
    }


    static void sort(List<? extends JDBCInterceptor> interceptors){
        Collections.sort(interceptors, new Comparator<JDBCInterceptor>() {
            public int compare(JDBCInterceptor r1, JDBCInterceptor r2) {
                int order1 = r1.order();
                int ordre2 = r2.order();
                if(order1 > ordre2){
                    return 1;
                }else if(order1 < ordre2){
                    return -1;
                }
                return 0;
            }
        });
    }
}
