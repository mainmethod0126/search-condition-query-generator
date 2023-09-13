# Search Condition Query Generator

This library makes it easy to convert a predefined Search Condition specification into query formats for databases like Elasticsearch and RDB.

## Usage

### RdbQueryGenerator

This feature has not been developed yet

---

### ElasticsearchQueryGenerator

Create an `org.elasticsearch.index.query.BoolQueryBuilder` object from the `com.google.gson.JsonElement` object.

Library users can benefit from this `BoolQueryBuilder` object for convenient Elasticsearch query construction.

#### sample

```java
String searchRequest = "{\"field\":\"user.name\",\"operator\":\"eq\",\"value\":\"shinwoosub\"}";
JsonElement json = (JsonElement) (new Gson()).fromJson(searchRequest, JsonElement.class);

// Create an `org.elasticsearch.index.query.BoolQueryBuilder` object from the `com.google.gson.JsonElement` object
BoolQueryBuilder boolQueryBuilder = generate(jsonElement);

SearchRequest searchRequest = this.index.createSearchRequest();
SearchSourceBuilder searchSourceBuilder = this.index.createSearchSourceBuilder().trackTotalHits(true)
        .query(boolQueryBuilder);

searchRequest.source(searchSourceBuilder);
SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
```