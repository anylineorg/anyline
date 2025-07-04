/*
 * Copyright 2006-2025 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anyline.data.interceptor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public interface JDBCInterceptor {

    /**
     * 执行顺序 最小的先执行
     * @return int
     */
    default int order() {
        return 10;
    }

    static void sort(List<? extends JDBCInterceptor> interceptors) {
        Collections.sort(interceptors, new Comparator<JDBCInterceptor>() {
            public int compare(JDBCInterceptor r1, JDBCInterceptor r2) {
                int order1 = r1.order();
                int order2 = r2.order();
                if(order1 > order2) {
                    return 1;
                }else if(order1 < order2) {
                    return -1;
                }
                return 0;
            }
        });
    }
}
