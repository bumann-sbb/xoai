# XOAI

What is XOAI?

XOAI is the most powerful and flexible OAI-PMH Java Toolkit (initially developed by [Lyncode](https://github.com/lyncode),
updated by [DSpace](https://github.com/DSpace)). XOAI contains common Java classes allowing to easily implement
[OAI-PMH](https://en.wikipedia.org/wiki/Open_Archives_Initiative_Protocol_for_Metadata_Harvesting) data and service providers.

## Usage

**Moving** (again): as XOAI is [no longer actively maintained by DSpace since 2019](https://github.com/DSpace/xoai/issues/72#issuecomment-557292929),
this fork by the [*Global Dataverse Community Consortium*](https://dataversecommunity.global) provides an updated 
version for the needs of and usage with the open source repository [Dataverse Software](https://dataverse.org).

This library is available from Maven Central, simply rely on the main POM:

```
<dependency>
    <groupId>io.gdcc</groupId>
    <artifactId>xoai</artifactId>
    <version>5.0.0</version>
</dependency>
```

## Release notes

### v5.0.0
This is a breaking changes release with a lot of new features, influenced by the usage of XOAI within Dataverse and other places.

#### 💔 BREAKING CHANGES
- Compatible with Java 11+ only
- Uses java.time API instead of java.util.Date
- Data Provider: Changes required to your `ItemRepository`, `Item` and `ItemIdentifier` implementations
- Service Provider: Changes required to your code using an `OAIClient`, as default implementation changed

#### 🌟 FEATURES
- Use the new `CopyElement` or `Metadata.copyFromStream()` to skip metadata XML processing, so pregenerated or cached
  data can be served from your `ItemRepository` implementation
- Use native JDK HTTP client for OAI requests in service provider, 
  extended with client builder and option to create unsafe SSL connections for testing
- Add total number of results (inspired by GBIF #8)

#### 🏹 BUG FIXES
- Sets now are properly compared, re-enabling `SetRepositoryHelper` to identify available sets
- Many new try-with-resources to mitigate memory leak risks
- The StAX XML components have been configured to avoid loading external entities, mitigating potential security risks
- And more...

## License

See [LICENSE](LICENSE) or [DSpace BSD License](https://raw.github.com/DSpace/DSpace/master/LICENSE)
