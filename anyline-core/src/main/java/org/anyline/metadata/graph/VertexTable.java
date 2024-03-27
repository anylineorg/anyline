package org.anyline.metadata.graph;

import java.io.Serializable;

/**
 * 为什么不用VertexType或VertexCollection,因为type容易误会成一个属性,collection容易误会成结果集
 */
public class VertexTable extends GraphTable implements Serializable {
}
