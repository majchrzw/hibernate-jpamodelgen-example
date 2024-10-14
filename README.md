Repo with reproducer of problem with Hibernate Jpamodelgen while using Kotlin. 
When using Jpamodelgen to generate metamodel you should be able to use generated metamodel classes in your code. 
In this example when using generated classes in your code as variable everything works as intended, which you can see on `main` branch of this repo.
When trying to use generated class in annotations like `@OneToMany(mappedBy=Entity_.FIELD)` 
error occurs on building project, which can be seen on `annotation` branch.