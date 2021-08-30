---
page_type: sample
languages:
- java
products:
- Azure Spring Cloud
description: "Deploy Spring microservices using Azure Spring Cloud and MySQL"
urlFragment: "spring-petclinic-microservices"
---

# Deploy Spring Boot applications - using Azure Spring Cloud Enterprise and MySQL 

Azure Spring Cloud enables you to easily run Spring Boot applications on Azure.

This quickstart shows you how to deploy existing Java Spring Boot applications to Azure. When 
you arre finished, you can continue to manage the application via the Azure CLI or switch to using the 
Azure Portal.

Note -  if you would like to deploy the same application to Azure Spring Cloud Standard, please go the
['azure' branch](https://github.com/Azure-Samples/spring-petclinic-microservices).

* [Deploy Spring Microservices using Azure Spring Cloud and MySQL](#deploy-spring-microservices-using-azure-spring-cloud-and-mysql)
  * [What will you experience](#what-will-you-experience)
  * [What you will need](#what-you-will-need)
  * [Install the Azure CLI extension](#install-the-azure-cli-extension)
  * [Clone and build the repo](#clone-and-build-the-repo)
  * [CREATE Azure Spring Cloud Enterprise service instance](#provision-azure-spring-cloud-enterprise-service-instance)
  * [CREATE microservice applications](#create-microservice-applications)
  * [Create MySQL Database](#create-mysql-database)
  * [DEPLOY applications and set environment variables](#deploy-applications-and-set-environment-variables)
  * [MONITOR microservice applications](#monitor-microservice-applications)
  * [Next Steps](#next-steps)

## What will you experience
You will:
- Build existing Spring microservices applications
- Provision an Azure Spring Cloud Enterprise service instance
- Deploy applications to Azure
- Bind applications to Azure Database for MySQL
- Open the application
- Monitor applications

## What you will need

In order to deploy Java apps to cloud, you need 
an Azure subscription. If you do not already have an Azure 
subscription, you can activate your 
[MSDN subscriber benefits](https://azure.microsoft.com/pricing/member-offers/msdn-benefits-details/) 
or sign up for a 
[free Azure account]((https://azure.microsoft.com/free/)).

In addition, you will need the following:

| [Azure CLI](https://docs.microsoft.com/cli/azure/install-azure-cli?view=azure-cli-latest) 
| [Java 8](https://www.azul.com/downloads/azure-only/zulu/?version=java-8-lts&architecture=x86-64-bit&package=jdk) 
| [Maven](https://maven.apache.org/download.cgi) 
| [MySQL CLI](https://dev.mysql.com/downloads/shell/)
| [Git](https://git-scm.com/)
|

## Install the Azure CLI extension

Install the Azure Spring Cloud "Enterprise" extension for the Azure CLI using the following command

```bash
    az extension remove -n spring-cloud
    az extension add -s https://ascprivatecli.blob.core.windows.net/enterprise/spring_cloud-2.7.0a1-py3-none-any.whl -y
```

## Clone and build the repo

### Create a new folder and clone the sample app repository to your Azure Cloud account  

```bash
    mkdir source-code
    git clone https://github.com/azure-samples/spring-petclinic-microservices
```

### Change directory and build the project

```bash
    cd spring-petclinic-microservices
    mvn clean package -DskipTests -Denv=cloud
```
This will take a few minutes.

## Provision Azure Spring Cloud Enterprise service instance

### Prepare your environment for deployments

Create a bash script with environment variables by making a copy of the supplied template:
```bash
    cp .scripts/setup-env-variables-azure-template.sh .scripts/setup-env-variables-azure.sh
```

Open `.scripts/setup-env-variables-azure.sh` and enter the following information:

```bash

    export SUBSCRIPTION=subscription-id # customize this
    export RESOURCE_GROUP=resource-group-name # customize this
    ...
    export SPRING_CLOUD_SERVICE=azure-spring-cloud-name # customize this
    ...
    export MYSQL_SERVER_NAME=mysql-servername # customize this
    ...
    export MYSQL_SERVER_ADMIN_NAME=admin-name # customize this
    ...
    export MYSQL_SERVER_ADMIN_PASSWORD=SuperS3cr3t # customize this
    ...
```

Then, set the environment:
```bash
    source .scripts/setup-env-variables-azure.sh
```

### Login to Azure 
Login to the Azure CLI and choose your active subscription. Be sure to choose the active subscription that is whitelisted for Azure Spring Cloud

```bash
    az login
    az account list -o table
    az account set --subscription ${SUBSCRIPTION}
```

### Create Azure Spring Cloud Enterprise service instance

Your subscription must be whitelisted to participate in the  Azure Spring Cloud Enterprise Private 
Preview program. If your subscription is not already whitelisted, sign up for the preview at 
[aka.ms/spring-cloud-enterprise](https://aka.ms/spring-cloud-enterprise) by clicking **Contact Me**.

Prepare a name for your Azure Spring Cloud service. The name must be between 4 and 32 
characters long and can contain only lowercase letters, numbers, and hyphens. 
The first character of the service name must be a letter and the last character must 
be either a letter or a number.

Create a resource group to contain your Azure Spring Cloud service.

```bash
    az group create --name ${RESOURCE_GROUP} \
        --location ${REGION}
```

Create an instance of Azure Spring Cloud using Azure Portal.

1. **Open** Azure Portal - [https://ms.portal.azure.com/?AppPlatformExtension=enterprise#create/Microsoft.AppPlatform](https://ms.portal.azure.com/?AppPlatformExtension=enterprise#create/Microsoft.AppPlatform)

1. In the pricing option, click **Change** and choose **Enterprise**

    ![](./media/create-azure-spring-cloud-enterprise-tier.jpg)

1. Check **Terms** checkbox to agree with legal terms and privacy statements of the Azure Spring Cloud
Enterprise tier offering in Azure Marketplace

    ![](./media/agree-to-terms.jpg)

1. Click **Next: VMware Tanzu settings>** button at the bottom right of the page to 
configure VMware Tanzu components

    > **NOTE**
    >
    > By default, Tanzu Service Registry and Tanzu Application Configuration Service are enabled. 
    Accept the defaults

    ![](./media/create-azure-spring-cloud-enterprise-tier-vmware-tanzu-ala-carte.jpg)

1. Click **Next: Diagnostic settings>** button at the bottom right 
of the page to configure Azure Monitor and Log Analytics. Choose an existing Log Analytics or 
create a new one

    ![](./media/azure-spring-cloud-enterprise-tier-setup-diagnostics.jpg)

1. Click **Next: Application Insights >** button at the bottom right of the page to configure
Application Insights. Choose an existing Application Insights or create a new one 
and set sampling rate, say 50.

    ![](./media/azure-spring-cloud-enterprise-tier-setup-application-insights.jpg)

1. Click **Review and create** button at the bottom left of the page. After validation is successfully 
completed, click **Create** button to start provisioning an Azure Spring Cloud Enterprise service 
instance. It takes about 5 minutes to provision

    ![](./media/create-azure-spring-cloud-enterprise-tier-validated.jpg)

1. Upon completion, go to the resource

    ![](./media/azure-spring-cloud-enterprise-tier.jpg)

    Set your default resource group name and cluster name using the following commands:
    
    ```bash
        az configure --defaults \
            group=${RESOURCE_GROUP} \
            location=${REGION} \
            spring-cloud=${SPRING_CLOUD_SERVICE}
    ```

### Load Tanzu Application Configuration Service

Use the `application.yml` in the root of this project to load configuration into the 
Tanzu Application Configuration Service in Azure Spring Cloud.

```bash
    az spring-cloud application-configuration-service \
        git repo add --label master --name petclinic \
        --patterns "api-gateway,customers-service/mysql,vets-service/mysql,visits-service/mysql,admin-server" \
        --uri "https://github.com/Azure-Samples/spring-petclinic-microservices-config"
```

## Create microservice applications

Create 5 microservice apps.

```bash
    az spring-cloud app create --name ${API_GATEWAY} --instance-count 1 --assign-endpoint \
        --memory 2Gi
    
    az spring-cloud app create --name ${ADMIN_SERVER} --instance-count 1 --assign-endpoint \
        --memory 2Gi
    
    az spring-cloud app create --name ${CUSTOMERS_SERVICE} --instance-count 1 \
        --memory 2Gi 
    
    az spring-cloud app create --name ${VETS_SERVICE} --instance-count 1 \
        --memory 2Gi
    
    az spring-cloud app create --name ${VISITS_SERVICE} --instance-count 1 \
        --memory 2Gi
```

## Bind applications to Tanzu Application Configuration Service and Tanzu Service Registry

Bind all your applications to Tanzu Application Configuration Service and Tanzu Service Registry.

```bash
    az spring-cloud application-configuration-service bind --app ${API_GATEWAY}
    az spring-cloud application-configuration-service bind --app ${ADMIN_SERVER}
    az spring-cloud application-configuration-service bind --app ${CUSTOMERS_SERVICE}
    az spring-cloud application-configuration-service bind --app ${VETS_SERVICE}
    az spring-cloud application-configuration-service bind --app ${VISITS_SERVICE}
    
    az spring-cloud service-registry bind --app ${API_GATEWAY}
    az spring-cloud service-registry bind --app ${ADMIN_SERVER}
    az spring-cloud service-registry bind --app ${CUSTOMERS_SERVICE}
    az spring-cloud service-registry bind --app ${VETS_SERVICE}
    az spring-cloud service-registry bind --app ${VISITS_SERVICE}
```

## Create MySQL Database

Create a MySQL database in Azure Database for MySQL.

```bash
    // create mysql server
    az mysql server create --resource-group ${RESOURCE_GROUP} \
     --name ${MYSQL_SERVER_NAME}  --location ${REGION} \
     --admin-user ${MYSQL_SERVER_ADMIN_NAME} \
     --admin-password ${MYSQL_SERVER_ADMIN_PASSWORD} \
     --sku-name GP_Gen5_2 \
     --ssl-enforcement Disabled \
     --version 5.7
    
    // allow access from Azure resources
    az mysql server firewall-rule create --name allAzureIPs \
     --server ${MYSQL_SERVER_NAME} \
     --resource-group ${RESOURCE_GROUP} \
     --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0
    
    // allow access from your dev machine for testing
    az mysql server firewall-rule create --name devMachine \
     --server ${MYSQL_SERVER_NAME} \
     --resource-group ${RESOURCE_GROUP} \
     --start-ip-address <ip-address-of-your-dev-machine> \
     --end-ip-address <ip-address-of-your-dev-machine>
    
    // increase connection timeout
    az mysql server configuration set --name wait_timeout \
     --resource-group ${RESOURCE_GROUP} \
     --server ${MYSQL_SERVER_NAME} --value 2147483
    
    // SUBSTITUTE values
    mysql -u ${MYSQL_SERVER_ADMIN_LOGIN_NAME} \
     -h ${MYSQL_SERVER_FULL_NAME} -P 3306 -p
    
    Enter password:
    Welcome to the MySQL monitor.  Commands end with ; or \g.
    Your MySQL connection id is 64379
    Server version: 5.6.39.0 MySQL Community Server (GPL)
    
    Copyright (c) 2000, 2018, Oracle and/or its affiliates. All rights reserved.
    
    Oracle is a registered trademark of Oracle Corporation and/or its
    affiliates. Other names may be trademarks of their respective
    owners.
    
    Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.
    
    mysql> CREATE DATABASE petclinic;
    Query OK, 1 row affected (0.10 sec)
    
    mysql> CREATE USER 'root' IDENTIFIED BY 'petclinic';
    Query OK, 0 rows affected (0.11 sec)
    
    mysql> GRANT ALL PRIVILEGES ON petclinic.* TO 'root';
    Query OK, 0 rows affected (1.29 sec)
    
    mysql> CALL mysql.az_load_timezone();
    Query OK, 3179 rows affected, 1 warning (6.34 sec)
    
    mysql> SELECT name FROM mysql.time_zone_name;
    ...
    
    mysql> quit
    Bye
    
    
    az mysql server configuration set --name time_zone \
     --resource-group ${RESOURCE_GROUP} \
     --server ${MYSQL_SERVER_NAME} --value "US/Pacific"
```

## Deploy applications and set environment variables

Deploy microservice applications to Azure.

```bash
    az spring-cloud app deploy --name ${API_GATEWAY} \
        --config-file-patterns ${API_GATEWAY} \
        --jar-path ${API_GATEWAY_JAR}
    
    
    az spring-cloud app deploy --name ${ADMIN_SERVER} \
        --config-file-patterns ${ADMIN_SERVER} \
        --jar-path ${ADMIN_SERVER_JAR} 
    
    
    az spring-cloud app deploy --name ${CUSTOMERS_SERVICE} \
        --config-file-patterns ${CUSTOMERS_SERVICE}/mysql \
        --jar-path ${CUSTOMERS_SERVICE_JAR} \
        --jvm-options='-Dspring.profiles.active=mysql' \
        --env MYSQL_SERVER_FULL_NAME=${MYSQL_SERVER_FULL_NAME} \
              MYSQL_DATABASE_NAME=${MYSQL_DATABASE_NAME} \
              MYSQL_SERVER_ADMIN_LOGIN_NAME=${MYSQL_SERVER_ADMIN_LOGIN_NAME} \
              MYSQL_SERVER_ADMIN_PASSWORD=${MYSQL_SERVER_ADMIN_PASSWORD}
    
    
    az spring-cloud app deploy --name ${VETS_SERVICE} \
        --config-file-patterns ${VETS_SERVICE}/mysql  \
        --jar-path ${VETS_SERVICE_JAR} \
        --jvm-options='-Dspring.profiles.active=mysql' \
        --env MYSQL_SERVER_FULL_NAME=${MYSQL_SERVER_FULL_NAME} \
              MYSQL_DATABASE_NAME=${MYSQL_DATABASE_NAME} \
              MYSQL_SERVER_ADMIN_LOGIN_NAME=${MYSQL_SERVER_ADMIN_LOGIN_NAME} \
              MYSQL_SERVER_ADMIN_PASSWORD=${MYSQL_SERVER_ADMIN_PASSWORD}
              
    
    az spring-cloud app deploy --name ${VISITS_SERVICE} \
        --config-file-patterns ${VISITS_SERVICE}/mysql \
        --jar-path ${VISITS_SERVICE_JAR} \
        --jvm-options='-Dspring.profiles.active=mysql' \
        --env MYSQL_SERVER_FULL_NAME=${MYSQL_SERVER_FULL_NAME} \
              MYSQL_DATABASE_NAME=${MYSQL_DATABASE_NAME} \
              MYSQL_SERVER_ADMIN_LOGIN_NAME=${MYSQL_SERVER_ADMIN_LOGIN_NAME} \
              MYSQL_SERVER_ADMIN_PASSWORD=${MYSQL_SERVER_ADMIN_PASSWORD}
```

```bash
    az spring-cloud app show --name ${API_GATEWAY} | grep url
```

Navigate to the URL provided by the previous command to open the Pet Clinic microservice application.
    
![](./media/petclinic.jpg)

## Monitor microservice applications

Open the Application Insights created by Azure Spring Cloud and start monitoring microservice applications.

Navigate to the `Application Map` blade:
![](./media/distributed-tracking-new-ai-agent.jpg)

Navigate to the `Performance` blade:
![](./media/petclinic-microservices-performance.jpg)

Navigate to the `Performance/Dependenices` blade - you can see the performance number for dependencies, 
particularly SQL calls:
![](./media/petclinic-microservices-insights-on-dependencies.jpg)

Click on a SQL call to see the end-to-end transaction in context:
![](./media/petclinic-microservices-end-to-end-transaction-details.jpg)

Navigate to the `Failures/Exceptions` blade - you can see a collection of exceptions:
![](./media/petclinic-microservices-failures-exceptions.jpg)

Click on an exception to see the end-to-end transaction and stacktrace in context:
![](./media/end-to-end-transaction-details.jpg)

Navigate to the `Metrics` blade - you can see metrics contributed by Spring Boot apps, 
Spring Cloud modules, and dependencies. 
The chart below shows `gateway-requests` (Spring Cloud Gateway), `hikaricp_connections`
 (JDBC Connections) and `http_client_requests`.
 
![](./media/petclinic-microservices-metrics.jpg)

Spring Boot registers a lot number of core metrics: JVM, CPU, Tomcat, Logback... 
The Spring Boot auto-configuration enables the instrumentation of requests handled by Spring MVC.
All those three REST controllers `OwnerResource`, `PetResource` and `VisitResource` have been instrumented by the `@Timed` Micrometer annotation at class level.

* `customers-service` application has the following custom metrics enabled:
  * @Timed: `petclinic.owner`
  * @Timed: `petclinic.pet`
* `visits-service` application has the following custom metrics enabled:
  * @Timed: `petclinic.visit`

You can see these custom metrics in the `Metrics` blade:
![](./media/petclinic-microservices-custom-metrics.jpg)

You can use the Availability Test feature in Application Insights and monitor 
the availability of applications:
![](./media/petclinic-microservices-availability.jpg)

Navigate to the `Live Metrics` blade - you can see live metrics on screen with low latencies < 1 second:
![](./media/petclinic-microservices-live-metrics.jpg)

## Next Steps

In this quickstart, you've deployed an existing Spring microservices app using Azure CLI, Terraform and GitHub Actions. To learn more about Azure Spring Cloud, go to:

- [Azure Spring Cloud](https://azure.microsoft.com/en-us/services/spring-cloud/)
- [Java on Azure](https://docs.microsoft.com/en-us/azure/java/)
- [Deploy Spring microservices from scratch](https://github.com/microsoft/azure-spring-cloud-training)
- [Deploy existing Spring microservices](https://github.com/Azure-Samples/azure-spring-cloud)
- [Azure for Java Cloud Developers](https://docs.microsoft.com/en-us/azure/java/)
- [Spring Cloud Azure](https://cloud.spring.io/spring-cloud-azure/)
- [Spring Cloud](https://spring.io/projects/spring-cloud)

## Credits

This Spring microservices sample is forked from 
[spring-petclinic/spring-petclinic-microservices](https://github.com/spring-petclinic/spring-petclinic-microservices) - see [Petclinic README](./README-petclinic.md). 

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
