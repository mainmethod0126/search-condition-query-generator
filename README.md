# Search Condition Query Generator

This library makes it easy to convert a predefined Search Condition specification into query formats for databases like Elasticsearch and RDB.

## Usage

### Add dependency

[Go to 🚀 maven central repository](https://central.sonatype.com/artifact/io.github.mainmethod0126/search-condition-query-generator)

#### for Gradle

```groovy
implementation group: 'io.github.mainmethod0126', name: 'search-condition-query-generator', version: '0.1.0'
```

- short
  
```groovy
implementation 'io.github.mainmethod0126:search-condition-query-generator:0.1.0'
```

- kotlin
  
```kotlin
implementation("io.github.mainmethod0126:search-condition-query-generator:0.1.0")
```

#### for Maven

```xml
<dependency>
    <groupId>io.github.mainmethod0126</groupId>
    <artifactId>search-condition-query-generator</artifactId>
    <version>0.1.0</version>
</dependency>
```

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