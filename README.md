# Btx框架

自用springboot脚手架


## Getting Started

JDk17+、Maven

### Prerequisites

None

### Installing

```
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>top.cheesetree.btx.framework</groupId>
                <artifactId>
                    btx-framework-dependencies
                </artifactId>
                <version>${btx.framework.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```

## Running the tests
None

### Break down into end to end tests

None
### And coding style tests

```
        <dependency>
            <groupId>top.cheesetree.btx.framework</groupId>
            <artifactId>btx-framework-boot-web</artifactId>
        </dependency>
        <dependency>
            <groupId>top.cheesetree.btx.framework</groupId>
            <artifactId>btx-framework-boot-cache</artifactId>
        </dependency>
```

## Deployment

```
maven deploy
```

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Contributing

None

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags).

## Authors

* **van** - *Initial work* - [wallichii](https://github.com/wallichii)

See also the list of [contributors](https://github.com/wallichii/btx-framework-root/contributors) who participated in this project.

## License

This project is licensed under the Apache2 License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments
None
