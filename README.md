## 图书管理系统

### 环境要求
- JDK 17.0.7
- Apache Maven 3.9.1
- MySQL Server 8.0.33

运行前，需要将src/resources/application_template.yaml复制一份放至**根目录**，并重命名为application.yaml，然后修改其中的数据库连接信息

清理输出目录并编译项目主代码
`mvn clean compile`

运行主代码
`mvn exec:java -Dexec.mainClass="Main" -Dexec.cleanupDaemonThreads=false`

- 注意：在Windows下，需要使用`mvn exec:java -D"exec.mainClass"="Main" -D"exec.cleanupDaemonThreads"=false`命令，来源参考[Unknown lifecycle phase on Maven](https://stackoverflow.com/questions/64299956/unknown-lifecycle-phase-on-maven)

运行所有的测试
`mvn -Dtest=LibraryTest clean test`

运行某个特定的测试
`mvn -Dtest=LibraryTest#parallelBorrowBookTest clean test`

打包成jar文件
`mvn clean package`