# TESTING.md

## Test Framework

**None.** This codebase has no test framework configured and no test files.

- No `src/test/` directory exists
- No test dependencies in `build.gradle`
- No JUnit, Mockito, or any testing library in the dependency graph
- No CI/CD pipeline configuration (no `.github/workflows/`, no Jenkinsfile, no CI config)

## Testing Strategy

**Manual testing only.** The development workflow relies on:
1. `./gradlew compileJava` — Compile-time verification
2. `./gradlew runClient` — Manual in-game testing
3. `./gradlew runData` — Verify datagen output

## Codebase Testability

| Aspect | Status | Notes |
|---|---|---|
| Unit testable logic | Limited | Most logic is tightly coupled to Minecraft classes |
| Capability testing | Difficult | Requires full Forge environment for `LazyOptional`, `ItemStack`, etc. |
| Packet testing | Difficult | Requires `NetworkEvent.Context`, server-side simulation |
| Render testing | Very difficult | Requires OpenGL context, Minecraft client bootstrap |
| Energy math | Testable | `FEBladeStorage.extractEnergy()` / `receiveEnergy()` are pure math — could be unit tested in isolation |
| NBT serialization | Testable | `serializeNBT()` / `deserializeNBT()` could be unit tested with sample CompoundTags |

## Validation Commands (from PROGRESS.md)

```
compileJava:
runData:
runClient:
```
All currently unvalidated — migration is at phase 0 baseline stage.

## Recommendations

1. **Energy math unit tests** — `FEBladeStorage` has the cleanest boundary for testing. Tests could verify:
   - Energy extraction never exceeds stored amount
   - Energy receipt never exceeds capacity
   - `isPowered` correctly set to false when energy reaches 0
   - `serializeNBT` / `deserializeNBT` roundtrip preserves all fields
2. **Recipe datagen validation** — `SlashBladeRecipeProvider` output could be validated against expected JSON schema
3. **Integration tests** — Consider Minecraft test mod or GameTest framework for in-game behavior verification
