# ProjectE-MITE

ProjectE ported to **MITE 1.6.4** via FishModLoader (FML v3.4.3, Fabric-style
`fml.mod.json` entrypoints + mixins, fml-loom Gradle build).

## Requirements

- JDK 17
- `1.6.4-MITE-HDS_FMLv3.4.3.jar` (MITE runtime with FML) — currently referenced
  by an absolute path in `build.gradle`; adjust before building.
- `RustedIronCore-1.5.5.jar`
- Third-party jars expected under `libs/` (not committed): `baubles-1.1.2.jar`,
  `pinin-lib-1.6.0.jar`, `gson.jar`, `commons-lang3.jar`.

## Build & run

```bash
./gradlew build
./gradlew runClient
```

## License

This is a port of [ProjectE](https://github.com/sinkillerj/ProjectE) (MIT).
Third-party libraries are not redistributed in this repository.
