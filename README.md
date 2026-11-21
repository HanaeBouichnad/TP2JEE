# TP2 JEE : Architecture micro-services e-commerce avec Spring Cloud

Application e-commerce basée sur une **architecture micro-services** (Spring Boot et Spring Cloud) : gestion des clients, des produits et des factures, avec un annuaire de services, une passerelle, une configuration centralisée, la communication entre services avec OpenFeign et la tolérance aux pannes avec Resilience4J.

Réalisé par **Hanae Bouichnad**, dans le cadre du module JEE.

---

## Architecture

```
                 Postman / Navigateur
                          |
                          v
              +-----------------------+
              |   GATEWAY-SERVICE     |  :8888   point d'entrée unique
              +-----------------------+
                 |        |         |
                 v        v         v
   +--------------+  +--------------+  +------------------+
   | CUSTOMER-    |  | INVENTORY-   |  | BILLING-         |
   | SERVICE      |  | SERVICE      |  | SERVICE          |
   | :8081        |  | :8082        |  | :8083            |
   | clients      |  | produits     |  | factures         |
   +--------------+  +--------------+  | OpenFeign +      |
          ^                 ^          | Circuit Breaker  |
          |                 |          +------------------+
          +-------- OpenFeign ---------------+
                                             
   +------------------------+     +------------------------+
   |  DISCOVERY-SERVICE     |     |  CONFIG-SERVICE        |
   |  :8761  annuaire       |     |  :9999  configuration  |
   |  Eureka                |     |  centralisée           |
   +------------------------+     +------------------------+
                                             |
                                             v
                                  dépôt GitHub configTP2JEE
```

| Service | Port | Rôle |
|---|---|---|
| **discovery-service** | 8761 | Annuaire Eureka : chaque service s'y inscrit avec son nom |
| **config-service** | 9999 | Serveur Spring Cloud Config : distribue la configuration lue sur GitHub |
| **customer-service** | 8081 | Gestion des clients, exposée avec Spring Data REST |
| **inventory-service** | 8082 | Gestion des produits, exposée avec Spring Data REST |
| **billing-service** | 8083 | Gestion des factures, appelle customer-service et inventory-service avec OpenFeign, Circuit Breaker Resilience4J |
| **gateway-service** | 8888 | Point d'entrée unique, routage dynamique à partir d'Eureka |

---

## Technologies

- **Java 17**, **Spring Boot 4**, **Spring Cloud**
- **Spring Data JPA**, **Spring Data REST**, **H2**, **Lombok**
- **Eureka** (Discovery Service), **Spring Cloud Gateway**
- **Spring Cloud Config** (configuration centralisée sur GitHub)
- **OpenFeign** (communication entre services), **Resilience4J** (Circuit Breaker)
- **Spring Boot Actuator** (endpoint `/actuator/refresh`)

---

## 1. Customer-Service

Les clients sont exposés automatiquement par **Spring Data REST** grâce à `@RepositoryRestResource`. La classe `RestRepositoryConfig` permet d'afficher l'`id` des clients dans les réponses JSON.

Tous les clients (`/customers`) :

<a href="screenShots/customerScreens/1-cutomers.png"><img src="screenShots/customerScreens/1-cutomers.png" alt="Liste des clients" width="600"></a>

Un client par son id (`/customers/1`) :

<a href="screenShots/customerScreens/2-customer-by-id.png"><img src="screenShots/customerScreens/2-customer-by-id.png" alt="Client par id" width="600"></a>

Pagination (`/customers?page=0&size=2`) :

<a href="screenShots/customerScreens/3-customers-pagination.png"><img src="screenShots/customerScreens/3-customers-pagination.png" alt="Pagination des clients" width="600"></a>

Tri (`/customers?sort=name,desc`) :

<a href="screenShots/customerScreens/4-customers-sort.png"><img src="screenShots/customerScreens/4-customers-sort.png" alt="Tri des clients" width="600"></a>

Base de données H2 :

<a href="screenShots/customerScreens/5-h2-console.png"><img src="screenShots/customerScreens/5-h2-console.png" alt="Console H2 clients" width="600"></a>

---

## 2. Inventory-Service

Les produits sont exposés de la même façon avec **Spring Data REST**.

Tous les produits (`/products`) :

<a href="screenShots/inventoryScreens/1-products.png"><img src="screenShots/inventoryScreens/1-products.png" alt="Liste des produits" width="600"></a>

Un produit par son id :

<a href="screenShots/inventoryScreens/2-product-by-id.png"><img src="screenShots/inventoryScreens/2-product-by-id.png" alt="Produit par id" width="600"></a>

Pagination :

<a href="screenShots/inventoryScreens/3-products-pagination.png"><img src="screenShots/inventoryScreens/3-products-pagination.png" alt="Pagination des produits" width="600"></a>

Tri :

<a href="screenShots/inventoryScreens/4-products-sort.png"><img src="screenShots/inventoryScreens/4-products-sort.png" alt="Tri des produits" width="600"></a>

Base de données H2 :

<a href="screenShots/inventoryScreens/5-h2-console.png"><img src="screenShots/inventoryScreens/5-h2-console.png" alt="Console H2 produits" width="600"></a>

---

## 3. Billing-Service

Chaque micro-service a sa propre base de données. La base de billing-service ne stocke donc que les **identifiants** du client et des produits. Pour afficher une facture complète, billing-service appelle customer-service et inventory-service avec **OpenFeign**.

### Endpoints Spring Data REST

Ils lisent uniquement la base de billing-service : le champ `customer` reste à `null`.

Toutes les factures (`/bills`) :

<a href="screenShots/billingScreens/1-bills.png"><img src="screenShots/billingScreens/1-bills.png" alt="Liste des factures" width="600"></a>

Une facture par son id (`/bills/1`) :

<a href="screenShots/billingScreens/2-bill-by-id.png"><img src="screenShots/billingScreens/2-bill-by-id.png" alt="Facture par id" width="600"></a>

Pagination :

<a href="screenShots/billingScreens/3-bills-pagination.png"><img src="screenShots/billingScreens/3-bills-pagination.png" alt="Pagination des factures" width="600"></a>

Tri :

<a href="screenShots/billingScreens/4-bills-sort.png"><img src="screenShots/billingScreens/4-bills-sort.png" alt="Tri des factures" width="600"></a>

Base de données H2 :

<a href="screenShots/billingScreens/5-h2-console.png"><img src="screenShots/billingScreens/5-h2-console.png" alt="Console H2 factures" width="600"></a>

Lignes de facture (`/productItems`) :

<a href="screenShots/billingScreens/6-product-items.png"><img src="screenShots/billingScreens/6-product-items.png" alt="Lignes de facture" width="600"></a>

### Communication entre services avec OpenFeign

Le contrôleur `BillRestController` (`/api/bills/{id}`) récupère la facture, puis appelle customer-service et inventory-service pour remplir le client et les produits :

<a href="screenShots/billingScreens/7-bill-details.png"><img src="screenShots/billingScreens/7-bill-details.png" alt="Facture complète avec OpenFeign" width="600"></a>

### Circuit Breaker (Resilience4J)

Quand customer-service est arrêté, billing-service ne plante pas : la méthode de secours (fallback) renvoie un client par défaut.

<a href="screenShots/billingScreens/8-circuit-breaker.png"><img src="screenShots/billingScreens/8-circuit-breaker.png" alt="Circuit Breaker" width="600"></a>

---

## 4. Discovery Service (Eureka)

Les services inscrits dans l'annuaire, avec leurs noms et leurs adresses :

<a href="screenShots/discoveryScreens/1-noms-et-adresses-des-services-enregistrés.png"><img src="screenShots/discoveryScreens/1-noms-et-adresses-des-services-enregistrés.png" alt="Eureka" width="600"></a>

---

## 5. Gateway

Les routes sont générées dynamiquement à partir d'Eureka : le nom du service est placé dans l'URL.

Customer-service via la Gateway (`/CUSTOMER-SERVICE/customers`) :

<a href="screenShots/gatewayScreens/1-gateway-customer-service.png"><img src="screenShots/gatewayScreens/1-gateway-customer-service.png" alt="Gateway clients" width="600"></a>

Inventory-service via la Gateway (`/INVENTORY-SERVICE/products`) :

<a href="screenShots/gatewayScreens/2-gateway-inventory-service.png"><img src="screenShots/gatewayScreens/2-gateway-inventory-service.png" alt="Gateway produits" width="600"></a>

Billing-service via la Gateway (`/BILLING-SERVICE/api/bills/1`) :

<a href="screenShots/gatewayScreens/3-gateway-billing-service.png"><img src="screenShots/gatewayScreens/3-gateway-billing-service.png" alt="Gateway factures" width="600"></a>

---

## 6. Configuration centralisée (Spring Cloud Config)

Les fichiers de configuration sont stockés dans un dépôt GitHub séparé : **[configTP2JEE](https://github.com/HanaeBouichnad/configTP2JEE)**.

| Fichier | Contenu |
|---|---|
| `application.properties` | Paramètres communs à tous les services (paramètres globaux, Eureka, Actuator, console H2) |
| `xxx-service.properties` | Paramètres propres à un service (profil par défaut) |
| `xxx-service-dev.properties` | Paramètres du profil `dev` |
| `xxx-service-prod.properties` | Paramètres du profil `prod` |

Paramètres globaux (`/application/default`) :

<a href="screenShots/configScreens/1-global-params.png"><img src="screenShots/configScreens/1-global-params.png" alt="Paramètres globaux" width="600"></a>

Paramètres de customer-service selon le profil :

<a href="screenShots/configScreens/2-customer-service-default-params.png"><img src="screenShots/configScreens/2-customer-service-default-params.png" alt="Profil par défaut" width="600"></a>

<a href="screenShots/configScreens/3-customer-service-dev-params.png"><img src="screenShots/configScreens/3-customer-service-dev-params.png" alt="Profil dev" width="600"></a>

<a href="screenShots/configScreens/4-customer-service-prod-params.png"><img src="screenShots/configScreens/4-customer-service-prod-params.png" alt="Profil prod" width="600"></a>

Lecture des paramètres dans customer-service avec `@Value` :

<a href="screenShots/configScreens/5-customer-service-config-test1.png"><img src="screenShots/configScreens/5-customer-service-config-test1.png" alt="Test de configuration 1" width="600"></a>

Lecture des paramètres avec `@ConfigurationProperties` (record `CustomerConfigParams`) :

<a href="screenShots/configScreens/6-customer-service-config-test2.png"><img src="screenShots/configScreens/6-customer-service-config-test2.png" alt="Test de configuration 2" width="600"></a>

---

## Lancer le projet

### Ordre de démarrage

1. `discovery-service`
2. `config-service`
3. `customer-service`
4. `inventory-service`
5. `billing-service`
6. `gateway-service`

### URLs utiles

| URL | Description |
|---|---|
| http://localhost:8761 | Tableau de bord Eureka |
| http://localhost:9999/application/default | Configuration globale |
| http://localhost:8888/CUSTOMER-SERVICE/customers | Clients via la Gateway |
| http://localhost:8888/INVENTORY-SERVICE/products | Produits via la Gateway |
| http://localhost:8888/BILLING-SERVICE/api/bills/1 | Facture complète via la Gateway |