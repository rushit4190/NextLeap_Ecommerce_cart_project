This is a basic E-commerce cart application-

There are three entities to be kept persistent - User, Product Catalog and Cart Catalog.

- User can sign up, sign in and sign out.
- Sign in generates a unique Session ID (UUID) which has timeout of 30 min
- User can view product catalog, search catalog based on search term.
- Cart is assigned to each user (upon adding the first product item) based on User Id.
- User can add product items, update quantity of a particular item, remove item and view all of the items present in the cart.

Application is designed to handle concurrency. Databases of User, Cart and Product are ensured to be persistent. SessionId data is not kept to be persistent.
User gets the sessionId on sign in, and uses it for cart operations.

The first part of this project (till MileStone 4) has CSV files as databases. It is a command line application.
The second part of this project (MileStone 5) is to introduce JDBC using SQLite Database. Data-Modeling and explanation can be found in Database Schema file.
The third and last part of this project (MileStone 7) is to define the REST API Specifications using Swagger/Open API that will be required to move this service to a Web Service.
In addition, the APIs defined in the last part are implemented using SpringBoot and tested using PostMan.

Project Structure - 
In src/main/java/Ecommerce package, there are following subpackages :-

- model - Definitions of User, Product, Cart and CartItem POJOs 
- service - Database operation interfaces and their concrete implementations (For CSV files and SQLite database) 
- controller - 

