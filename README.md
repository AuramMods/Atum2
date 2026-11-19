# [Atum 2]

Download on [CurseForge](https://minecraft.curseforge.com/projects/atum)

## Upgrade a 1.16.5 Atum world

Close the world in Minecraft 1.16.5 and keep a backup. From a checkout of this port with Java 17 installed, run the migration task with the original save directory followed by a new destination in an existing directory.

```sh
./gradlew migrateLegacyWorld --args='"/path/to/old save" "/path/to/new save"'
```

The tool copies the save, replaces Atum's legacy dimension generator, preserves Atum biome and mob spawner data, and moves legacy chunk entities into the entity files Minecraft 1.20.1 reads. Start Forge 1.20.1 with Atum and Curios using the converted directory. Unexplored chunks use the port's world generation.

How to get Atum 2 through maven
---
Add to your build.gradle:
```gradle
repositories {
  maven {
    // url of the maven that hosts Atum 2 files
    url "https://girafi.dk/maven/"
  }
}

dependencies {
  // compile against Atum 2
  compile fg.deobf("com.teammetallurgy.atum:atum2_${mc_version}:${mc_version}-${atum2_version}")
}
```

`${mc_version}` & `${atum2_version}` can be found [here](https://girafi.dk/maven/com/teammetallurgy/atum/), check the file name of the version you want.
