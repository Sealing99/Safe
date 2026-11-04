# Safe
Safe is a very lightweight economy plugin inspired by vault.
As Safe is an API, this plugin would be used as dependency to other plugins to easily manage balance systems of players using SQLite.


# Depend on Safe
To use Safe you need other plugins supporting it. Here is how to add Safe as a dependency to your PaperMC project using Gradle.

## Gradle - manual
1. Download a Safe .jar file.
2. Put the file in your development folder inside both ```/libs/``` and ```/run/plugins/```.
3. In gradle.properties, add this line: ```safe.version=[VERSION]```. (Change [VERSION] to your actual downloaded file version)
4. Next, add it to your dependencies in ```build.gradle(.kts)```. It would look something like:
   ```
   dependencies {
       compileOnly(files("libs/Safe-${property("safe.version")}.jar"))
   }
   ```
5. In your plugin's ```plugin.yml``` file, add ```Safe``` to your dependencies. It would look like:
   ```depend: [Safe]```
6. Reload your Gradle project.

## Gradle - GitHub
1. In your build.gradle(.kts), add these lines to ```repositories```:
   ```
   ivy {
       url = uri(
           "https://github.com/Sealing99/Safe/releases/download"
       )
   
       patternLayout {
           artifact("[revision]/[artifact]-[revision].[ext]")
       }

       metadataSources {
           artifact()
       }
   }
   ```
2. Next, add it to your dependencies: ```compileOnly("com.github.Sealing99:Safe:${property("safe.version")}")```
3. In gradle.properties, add ```safe.version=[VERSION]``` (replace [VERSION] with your actual version)
4. In your plugin's ```plugin.yml``` file, add ```Safe``` to your dependencies. It would look like:
   ```depend: [Safe]```
5. Reload your Gradle project.

## Gradle - jitpack
1. In your build.gradle(.kts), add ```maven { url = uri("https://jitpack.io") }``` at the end of ```repositories {}```.
2. Add ```compileOnly("com.github.Sealing99:Safe:${property("safe.version")}")``` to ```dependencies {}```.
3. In gradle.properties, add ```safe.version=[VERSION]``` (replace [VERSION] with your actual version)
4. In your plugin's ```plugin.yml``` file, add ```Safe``` to your dependencies. It would look like:
   ```depend: [Safe]```
5. Reload your Gradle project.

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
