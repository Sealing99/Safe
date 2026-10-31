# Safe
Safe is a very lightweight economy plugin inspired by vault.
As Safe is an API, this plugin would be used as dependency to other plugins to easily manage balance systems of players using SQLite.


# Depend on Safe
To use Safe you need other plugins supporting it. Here is how to add Safe as a dependency to your PaperMC project using Gradle.

## Gradle
1. Download a Safe .jar file.
2. Put the file in your development folder inside both ```/libs/``` and ```/run/plugins/```.
3. In your plugin's ```plugin.yml``` file, add ```Safe``` to your dependencies. It would look like:
   ```depend: [Safe]```
4. In gradle.properties, add this line: ```dependencies.Safe.library=libs/Safe-[VERSION].jar```. (Change [VERSION] to your actual downloaded file version)
5. Next, add it do your dependencies in ```build.gradle(.kts)```. It would look something like:
   ```
   dependencies {
       ...
       compileOnly(files("libs/Safe-0.1.0.jar"))
   }
   ```
6. Reload your Gradle project.

## How to use Safe as an API
Safe has exposed several methods to call to connect to the economy database.

1. Add a ```net.sealing99.safe.Safe``` field to your main class.
2. On enabling, initialize the field using:
   ```
   try {
       safeAPI = (Safe) getServer().getPluginManager().getPlugin("Safe");
   } catch (Exception e) {
       getLogger().severe("Failed to connect to Safe API");
       e.printStackTrace();
       getServer().getPluginManager().disablePlugin(this);
       return;
   }
   ```
3. To get a balance, call ```Safe#getBalanceDatabase().getBalance(String uuid)```
4. To set/add/remove money:
   ```
   Safe#getBalanceDatabase()
            .setBalance(String uuid, BigDecimal bal);
            .addMoney(String uuid, BigDecimal amount);
            .removeMoney(String uuid, BigDecimal amount);
   ```
##
Safe automatically saves/loads balances on start/stop, so no further functions to call are needed.
