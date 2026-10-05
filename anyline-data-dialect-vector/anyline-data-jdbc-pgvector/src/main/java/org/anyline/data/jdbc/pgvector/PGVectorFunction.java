/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.data.jdbc.pgvector;

import org.anyline.metadata.SystemFunction;
import org.anyline.metadata.type.DatabaseType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * pgvector 向量函数/操作符映射<br/>
 * 参考 https://github.com/pgvector/pgvector<br/>
 * <ul>
 *     <li>&lt;-&gt;  L2距离(欧氏距离)        vector</li>
 *     <li>&lt;=&gt;  余弦距离                vector</li>
 *     <li>&lt;#&gt;  负内积                  vector</li>
 *     <li>&lt;+&gt;  L1距离(曼哈顿距离)      vector 0.7.0+</li>
 *     <li>&lt;~&gt;  汉明距离                bit    0.7.0+</li>
 *     <li>&lt;%&gt;  Jaccard距离            bit    0.7.0+</li>
 * </ul>
 * 操作符形式可命中 HNSW/IVFFlat 索引(ORDER BY embedding &lt;-&gt; '[...]' LIMIT n),
 * 同名函数形式(l2_distance/cosine_distance/...)用于 SELECT 列表等不允许裸操作符的场景
 */
public enum PGVectorFunction implements SystemFunction {

    /* *********************************************************************************************
     *                                          距离操作符
     ***********************************************************************************************/
    /** L2距离(欧氏距离) */
    L2                  (VECTOR.L2,                 "${left} <-> ${right}"),
    /** 余弦距离 */
    COSINE              (VECTOR.COSINE,             "${left} <=> ${right}"),
    /** 负内积(值越小越相似) */
    NEGATIVE_INNER      (VECTOR.NEGATIVE_INNER,     "${left} <#> ${right}"),
    /** L1距离(曼哈顿距离) */
    L1                  (VECTOR.L1,                 "${left} <+> ${right}"),
    /** 汉明距离(bit类型) */
    HAMMING             (VECTOR.HAMMING,            "${left} <~> ${right}"),
    /** Jaccard距离(bit类型) */
    JACCARD             (VECTOR.JACCARD,            "${left} <%> ${right}"),

    /* *********************************************************************************************
     *                                          距离函数(与操作符等价)
     ***********************************************************************************************/
    L2_DISTANCE         (VECTOR.L2_DISTANCE,        "l2_distance(${left}, ${right})"),
    COSINE_DISTANCE     (VECTOR.COSINE_DISTANCE,    "cosine_distance(${left}, ${right})"),
    INNER_PRODUCT       (VECTOR.INNER_PRODUCT,      "inner_product(${left}, ${right})"),
    L1_DISTANCE         (VECTOR.L1_DISTANCE,        "l1_distance(${left}, ${right})"),
    HAMMING_DISTANCE    (VECTOR.HAMMING_DISTANCE,   "hamming_distance(${left}, ${right})"),
    JACCARD_DISTANCE    (VECTOR.JACCARD_DISTANCE,   "jaccard_distance(${left}, ${right})"),

    /* *********************************************************************************************
     *                                          相似度
     ***********************************************************************************************/
    /** 余弦相似度 = 1 - 余弦距离 */
    COSINE_SIMILARITY   (VECTOR.COSINE_SIMILARITY,  "1 - (${left} <=> ${right})"),
    /** L2相似度 = 1 / (1 + L2距离) */
    L2_SIMILARITY       (VECTOR.L2_SIMILARITY,      "1 / (1 + (${left} <-> ${right}))"),

    /* *********************************************************************************************
     *                                          向量工具函数
     ***********************************************************************************************/
    VECTOR_DIMS         (VECTOR.VECTOR_DIMS,        "vector_dims(${vector})"),
    VECTOR_NORM         (VECTOR.VECTOR_NORM,        "vector_norm(${vector})"),
    L2_NORMALIZE        (VECTOR.L2_NORMALIZE,       "l2_normalize(${vector})"),
    BINARY_QUANTIZE     (VECTOR.BINARY_QUANTIZE,    "binary_quantize(${vector})"),
    SUBVECTOR           (VECTOR.SUBVECTOR,          "subvector(${vector}, ${start}, ${len})"),
    ;

    /**
     * pgvector 向量运算元数据(标准名称 → 各数据库可各自实现)
     */
    public enum VECTOR implements SystemFunction.META {
        L2                  ("L2距离(欧氏距离)",       "left,right"),
        COSINE              ("余弦距离",                "left,right"),
        NEGATIVE_INNER      ("负内积",                  "left,right"),
        L1                  ("L1距离(曼哈顿距离)",      "left,right"),
        HAMMING             ("汉明距离(bit)",           "left,right"),
        JACCARD             ("Jaccard距离(bit)",        "left,right"),
        L2_DISTANCE         ("L2距离(函数)",            "left,right"),
        COSINE_DISTANCE     ("余弦距离(函数)",          "left,right"),
        INNER_PRODUCT       ("内积(函数)",              "left,right"),
        L1_DISTANCE         ("L1距离(函数)",            "left,right"),
        HAMMING_DISTANCE    ("汉明距离(函数)",          "left,right"),
        JACCARD_DISTANCE    ("Jaccard距离(函数)",       "left,right"),
        COSINE_SIMILARITY   ("余弦相似度",              "left,right"),
        L2_SIMILARITY       ("L2相似度",                "left,right"),
        VECTOR_DIMS         ("向量维度",                "vector"),
        VECTOR_NORM         ("向量范数",                "vector"),
        L2_NORMALIZE        ("L2归一化",                "vector"),
        BINARY_QUANTIZE     ("二值量化",                "vector"),
        SUBVECTOR           ("子向量",                  "vector,start,len"),
        ;

        private final String title;
        private final List<String> params;

        VECTOR(String title, String params) {
            this.title = title;
            this.params = Arrays.asList(params.split(","));
        }

        @Override
        public String title() {
            return title;
        }

        @Override
        public Category category() {
            return Category.MATH;
        }

        @Override
        public DatabaseType database() {
            return DatabaseType.PGVector;
        }

        @Override
        public List<String> params() {
            return params;
        }
    }

    @Override
    public META meta() {
        return meta;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String formula() {
        return formula;
    }

    /**
     * 本枚举在pgvector下的函数名, 用于按名称查找(操作符取操作符本身)
     */
    public String define() {
        return define;
    }

    @Override
    public DatabaseType database() {
        return DatabaseType.PGVector;
    }

    @Override
    public List<String> params() {
        return params;
    }

    @Override
    public void params(List<String> params) {
        this.params = params;
    }

    @Override
    public boolean support() {
        return support;
    }

    @Override
    public void support(boolean support) {
        this.support = support;
    }

    private final META meta;
    private final String title;
    private final String formula;
    private final String define;
    private List<String> params = new ArrayList<>();
    private boolean support = true;

    PGVectorFunction(META meta, String formula) {
        this.meta = meta;
        this.title = meta.name();
        this.define = meta.name();
        this.formula = formula;
    }
}