# transaction-processor

### 1. Problem Statement
Create a transaction processing system capable of following: 
1. Call an API to fetch transaction
2. Convert the transactions with their amounts mapped to a different currency
3. POST the converted transaction to another system for processing

### 2. Challenges

The system should be scalable as there can be a huge number of transactions that need to be processed.

It should also be performant i.e. It should do work in parallel where it can rather than do it sequentially.

As it looks like, the problem involves integration of multiple external systems. 

Integration of external systems poses different challenges:

#### 2.1 Unreliable systems
One of the API which we will be depending on in this problem will be currency exchange API. 

We have to design our system in a way that even if the API doesn't behave properly, our system
should gracefully handle it and keep a record of instances when the call to an API returned an error.
    
#### 2.2 Rate Limitations
External systems have their own capacity and limitations. So we cannot expect them to be always available 
and serve each and every request.  
For instance, exchange rate provider service can be a paid service which charges based on number of requests per minute
or based on total number of requests sent to it.

It can also send `429 Too Many Requests` if it implements rate limiting.

Therefore we need to be careful while integrating with other systems i.e. 
not sending uncontrolled number of requests in short periods of time

#### 2.3 Interchangeability
The system should not be tightly coupled to a particular service provider.

Interchangeability in our system will help us achieve following goals with the help of configuration change:

- Switching between different transaction provider systems
- Switch between different transaction processing systems
- Switching between different exchange rate APIs

#### 2.4 Performance
The transactions should be processed concurrently 
and the number of transactions processed should be controllable by a configuration parameter

### 3. Solution

The solution uses following technologies to address the challenges discussed above: 

- **Spring** for Dependency Injection for dependency injection 
- **Kotlin Coroutines** for asynchronous operations
- **Bucket4j** for rate limiting API calls

#### 3.1 Design

The system has been modelled using object oriented design techniques. Here is the list of major components of the system:

- **ExchangeRatesProvider**
- **TransactionProvider**
- **TransactionConverter**
- **TransactionProcessor**
- **CurrencyConverter** 


#### 3.3 How to run
`./gradlew bootRun`


#### 3.4 Configuration
All configuration parameters required to run the program are listed in `src/main/resources/application.properties`

Here is the list of parameters with their descriptions:

- `api.exchange.url` - URL for the exchange rate provider API
- `api.process.url` - URL where converted transactions are POSTed
- `api.transaction.url` - URL for API which needs to be called for fetching transactions
- `processor.chunk-size` - an integer which determines the size of chunk of transactions processed simultaenously.
- `processor.target` - sets number of transactions to be processed
