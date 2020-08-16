**ES7不再支持type**

## Mapping
**创建索引时就一起创建mapping：**

```
PUT /lib/
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 0
  },
  "mappings": {
    "books": {
      "properties": {
        "title": {
          "type": "text",
          "fields": {
            "test": {
              "type": "keyword"
            },
            "test2": {
              "type": "date"
            }
          }
        },
        "name": {
          "type": "text",
          "index": false
        },
        "publish_date": {
          "type": "date",
          "index": false
        },
        "price": {
          "type": "double"
        },
        "number": {
          "type": "integer"
        }
      }
    }
  }
}
```

说明：title本身用于检索，添加字段test类型为keyword用于排序、test2为date


**对于多层嵌套类型mapping：**

嵌套

```
POST /lib/user/
{
    "last_name" :  {"test1":{"test2":"www"}}
}
```

对应的mapping:

```
{
  "lib": {
    "mappings": {
      "user": {
        "properties": {
          "last_name": {
            "properties": {
              "test1": {
                "properties": {
                  "test2": {
                    "type": "text",
                    "fields": {
                      "keyword": {
                        "type": "keyword",
                        "ignore_above": 256
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
```

**支持的数据类型：**

1. 核心数据类型（Core datatypes）

    字符型：string，string类型包括
    text 和 keyword
    
    text类型被用来索引长文本，在建立索引前会将这些文本进行分词，转化为词的组合，建立索引。允许es来检索这些词语。text类型不能用来排序和聚合。
    
    Keyword类型不需要进行分词，可以被用来检索过滤、排序和聚合。keyword 类型字段只能用本身来进行检索
    
    数字型：long, integer, short, byte, double, float
    日期型：date
    布尔型：boolean
    二进制型：binary
    
2. 复杂数据类型（Complex datatypes）

    数组类型（Array datatype）：数组类型不需要专门指定数组元素的type，例如：
        字符型数组: [ "one", "two" ]
        整型数组：[ 1, 2 ]
        数组型数组：[ 1, [ 2, 3 ]] 等价于[ 1, 2, 3 ]
        对象数组：[ { "name": "Mary", "age": 12 }, { "name": "John", "age": 10 }]
    对象类型（Object datatype）：_ object _ 用于单个JSON对象；
    嵌套类型（Nested datatype）：_ nested _ 用于JSON数组；
关于Object 和Nested 点击

3. 地理位置类型（Geo datatypes）

    地理坐标类型（Geo-point datatype）：_ geo_point _ 用于经纬度坐标；
    地理形状类型（Geo-Shape datatype）：_ geo_shape _ 用于类似于多边形的复杂形状；

4. 特定类型（Specialised datatypes）

    IPv4 类型（IPv4 datatype）：_ ip _ 用于IPv4 地址；
    Completion 类型（Completion datatype）：_ completion _提供自动补全建议；
    Token count 类型（Token count datatype）：_ token_count _ 用于统计做了标记的字段的index数目，该值会一直增加，不会因为过滤条件而减少。
    mapper-murmur3
    类型：通过插件，可以通过 _ murmur3 _ 来计算 index 的 hash 值；
    附加类型（Attachment datatype）：采用 mapper-attachments
    插件，可支持_ attachments _ 索引，例如 Microsoft Office 格式，Open Document 格式，ePub, HTML 等。
    
支持的属性：

"store":false//是否单独设置此字段的是否存储而从_source字段中分离，默认是false，只能搜索，不能获取值

"index": true//分词，不分词是：false
   ，设置成false，字段将不会被索引
   
"analyzer":"ik"//指定分词器,默认分词器为standard analyzer

"boost":1.23//字段级别的分数加权，默认值是1.0

"doc_values":false//对not_analyzed字段，默认都是开启，分词字段不能使用，对排序和聚合能提升较大性能，节约内存

"fielddata":{"format":"disabled"}//针对分词字段，参与排序或聚合时能提高性能，不分词字段统一建议使用doc_value

"fields":{"raw":{"type":"string","index":"not_analyzed"}} //可以对一个字段提供多种索引模式，同一个字段的值，一个分词，一个不分词
            
"ignore_above":100 //超过100个字符的文本，将会被忽略，不被索引

"include_in_all":ture//设置是否此字段包含在_all字段中，默认是true，除非index设置成no选项

"index_options":"docs"//4个可选参数docs（索引文档号） ,freqs（文档号+词频），positions（文档号+词频+位置，通常用来距离查询），offsets（文档号+词频+位置+偏移量，通常被使用在高亮字段）分词字段默认是position，其他的默认是docs

"norms":{"enable":true,"loading":"lazy"}//分词字段默认配置，不分词字段：默认{"enable":false}，存储长度因子和索引时boost，建议对需要参与评分字段使用 ，会额外增加内存消耗量

"null_value":"NULL"//设置一些缺失字段的初始化值，只有string可以使用，分词字段的null值也会被分词

"position_increament_gap":0//影响距离查询或近似查询，可以设置在多值字段的数据上火分词字段上，查询时可指定slop间隔，默认值是100

"search_analyzer":"ik"//设置搜索时的分词器，默认跟ananlyzer是一致的，比如index时用standard+ngram，搜索时用standard用来完成自动提示功能

"similarity":"BM25"//默认是TF/IDF算法，指定一个字段评分策略，仅仅对字符串型和分词类型有效

"term_vector":"no"//默认不存储向量信息，支持参数yes（term存储），with_positions（term+位置）,with_offsets（term+偏移量），with_positions_offsets(term+位置+偏移量) 对快速高亮fast vector highlighter能提升性能，但开启又会加大索引体积，不适合大数据量用

**映射的分类：**

1. 动态映射：

 + 当ES在文档中碰到一个以前没见过的字段时，它会利用动态映射来决定该字段的类型，并自动地对该字段添加映射。
 
   可以通过dynamic设置来控制这一行为，它能够接受以下的选项：
   
   true：默认值。动态添加字段
    
   false：忽略新字段
    
   strict：如果碰到陌生字段，抛出异常

dynamic设置可以适用在根对象上或者object类型的任意字段上。

给索引lib2创建映射类型

```
PUT /lib/
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 0
  },
  "mappings": {
    "books": {
      "properties": {
        "title": {
          "type": "text"
        },
        "name": {
          "type": "text",
          "index": false
        },
        "publish_date": {
          "type": "date",
          "index": false
        },
        "price": {
          "type": "double"
        }
      }
    }
  }
}
```

增加索引字段（只能在原基础上添加，mapping不能修改）

```
PUT /lib/books/_mapping/
{
        "properties":{
            "titles":{"type":"text"},
            "books":{"type":"text"},
            "titles3":{"type":"text"}
        }
}
```
