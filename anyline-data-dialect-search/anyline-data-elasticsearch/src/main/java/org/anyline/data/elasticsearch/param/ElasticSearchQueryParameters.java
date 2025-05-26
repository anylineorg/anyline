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

package org.anyline.data.elasticsearch.param;

import java.util.Date;

public class ElasticSearchQueryParameters {
    protected Boolean allow_no_indices;
    protected Boolean allow_partial_search_results;
    protected String analyzer;
    protected Boolean analyze_wildcard;
    protected Integer batched_reduce_size;
    protected Boolean ccs_minimize_roundtrips;
    protected String default_operator;
    protected String df;
    protected String docvalue_fields;
    protected String expand_wildcards;
    protected Boolean explain;
    protected Integer from;
    protected Integer size;
    protected Boolean ignore_throttled;

    protected Boolean include_named_queries_score;
    protected Boolean ignore_unavailable;
    protected Boolean lenient;
    protected Integer max_concurrent_shard_requests;
    protected Integer pre_filter_shard_size;
    protected Boolean request_cache;
    protected Boolean rest_total_hits_as_int;
    protected String routing;
    protected Date scroll;
    //query_then_fetch dfs_query_then_fetch
    protected String search_type;
    protected Boolean seq_no_primary_term;
    protected String sort;
    // true false 属性
    protected Object _source;
    protected String _source_excludes;
    protected String _source_includes;
    protected String stats;
    protected String stored_fields;
    protected String suggest_field;
    protected Integer suggest_size;
    protected String suggest_text;
    protected Integer terminate_after;
    protected Long timeout;
    protected Boolean track_scores;
    protected Object track_total_hits;
    protected Boolean typed_keys;
    protected Boolean version;
}
