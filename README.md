# Device Cloud Jenkins Pipelines

This repository contains scripted Jenkins pipelines for several projects within Device Cloud of Testbirds.

We at Testbirds believe that this could be helpful to other people who are looking to create their own scripted pipelines for Jenkins, as it is not the most trivial task and the amount of examples available online is not so large.

## Structure

With this project we aimed to separate the code for the actual product from the infrastructure code. This allowed our product code to be completely agnostic about our infrastructure. It can be a little more challenging to achieve with Jenkins, where the default way of doing things is including a `Jenkinsfile` in the project code and writing the pipeline there.

We took a different approach and wrote all the pipelines in a single project that serves as a starting point for all jobs. It then subsequently checks out the code from our Git builds it, tests it and triggers the jobs that are dependent on the current job.

The structure of the project is the following:

- [`pipelines`](pipelines) directory contains the scripts that serve as entry points and definitions for the appropriate jobs
- [`src/de/testbirds`](src/de/testbirds) is a package with 3 classes for job configuration and job triggering
- [`vars`](vars) contains the helper functions and our custom stages
- [`dependencies.txt`](resources/dependencies.txt) describes the dependencies between the jobs

## Example

In this section we will dissect one example job and explain how it works in more detail. The job we will look at is Java API of TestChameleon.

The starting point of the job is the script at [`pipelines/tech-api-tools/tech-api/api-1.0.x`](pipelines/tech-api-tools/tech-api/api-1.0.x). First of all, the pipeline does some setup for the job in the `properties` function and then calls `techpipeline.pipe`, which is a wrapper around the call to `node` with some extra functionality like emails in case the job fails.

The stages are passed to the `pipe` function as a closure. The syntax may look a bit confusing, but Groovy allows putting the [closure outside of the parentheses](https://groovy-lang.org/style-guide.html#_omitting_parentheses) in a function call if it is the last parameter. Our pipelines use this syntax extensively. For more information about closures you can have a look [here](https://groovy-lang.org/closures.html).

The closure starts off by creating a new `PipelineConfig` with the configuration this project, which is then passed to the stages (as a closure as well).

The stages themselves are defined in the [`vars`](vars) directory. This particular project makes use of the 3 of our custom steps:

- [`techCheckoutStage`](vars/techCheckoutStage.groovy) that loads the code from our GitLab repo
- [`techBuildStage`](vars/techBuildStage.groovy) that builds a _jar_ and pushes it to Artifactory server
- [`techAnalyzeStage`](vars/techAnalyzeStage.groovy) that analyzes the code and publishes the reports

All our custom stages can also trigger the dependent jobs if they are set up to be the _trigger_ stage. In this case the _trigger_ stage if the default `build` stage. This allows us ot trigger the dependent jobs before the current job finishes. The `analyze` stage takes the longest to run and there is no need to wait for it's completion, as the _jar_ is available after the `build` stage and can be already used by the dependent jobs.

## Multibranch

Unfortunately we did not find a simple way how to properly create a multibranch pipeline completely separated from the project code. Our multibranch pipelines like [`grdn-connect-android`](pipelines/grdn/grdn-connect-android) still need a Jenkinsfile to work properly. This project is added as a common library and is loaded in the `Jenkinsfile` of the project like the following:

```groovy
@Library('tech-pipeline@master') _
multibranch.loadPipeline('master')
```

## Conclusion

This project is an example of how infrastructure code can be kept separately from product code. It is not a trivial task to create a CI/CD pipeline from scratch and we hope that open-sourcing our working solution could help someone to solve similar problems in their infrastructure.
