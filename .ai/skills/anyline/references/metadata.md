# 元数据读取操作

## 概述
本文档涵盖通过 Anyline MDM执行对表、列、视图、索引、约束、主外键、存储过程、函数、触发器等数据库对象信息查询以及结构差异对比
查询对象信息通过service.metadata()或ServiceProxy.metadata()执行

## 1.数据库对象
父类:zz   
表:class Table<E extends Table> extends Metadata<E> 
视图:class View extends Table<View>
图表父类:class GraphTable extends Table<GraphTable>
class EdgeTable extends GraphTable