This is a basic E-commerce cart application-

There are three entities to be kept persistent - User, Product Catalog and Cart Catalog.

- User can sign up, sign in and sign out.
- Sign in generates a unique Session ID (UUID) which has timeout of 30 min
- User can view product catalog, search catalog based on search term.
- Cart is assigned to each user (upon adding the first product item) based on User Id.
- User can add product items, update quantity of a particular item, remove item and view all of the items present in the cart.

Application is designed to handle concurrency. Databases of User, Cart and Product are ensured to be persistent.

The first commit of this project (till MileStone 4) has CSV files as databases. It is a command line application.
