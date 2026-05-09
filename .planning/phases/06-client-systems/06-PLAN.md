# Phase 6 Plan: Client Systems Migration

**Phase:** 6
**Goal:** Migrate renderer, input handling, keybindings, model baking, and JEI compat to NeoForge 1.21.1 client.
**Mode:** mvp (vertical slices)
**Date:** 2026-05-09

## Frontmatter

| Field | Value |
|-------|-------|
| phase | 6 |
| requirements | CLI-01, CLI-02, CLI-03, CLI-04, CLI-05, CLI-06, CLI-07 |
| files_modified | ClientSetupHandler.java, InputHandler.java, EnergyBladeBEWLR.java, ItemFEBlade.java, JEICompat.java |
| autonomous | false |
| depends_on | Phase 4 (Networking), Phase 5 (Datagen) |

## Prerequisite Check

Before executing any task, verify:
- [ ] Phase 4 networking is complete (`PowerSwitchPacket` and `NetworkPacketHandler` compile)
- [ ] Phase 5 datagen is complete (`./gradlew runData` succeeds)
- [ ] `docs/migration/LOADER_API_MAP.md` entries LAM-01 through LAM-51 confirmed
- [ ] SlashBlade NeoForge port is available (required for `ItemSlashBlade`, `BLADESTATE`, `SlashBladeTEISR`, `BladeModel`)

---

## Wave 1 — Parallel Client File Migrations

All 5 files modified in parallel — no inter-file dependencies within this wave.

### Task 1: ItemFEBlade.java — Client API Migration (CLI-01, CLI-02 partial)

**Requirements:** CLI-01, CLI-02

**Impacted file:** `src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java`

<read_first>
- src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java
- .planning/phases/06-client-systems/06-RESEARCH.md
- .planning/phases/06-client-systems/06-VANILLA-RESEARCH.md
- docs/migration/LOADER_API_MAP.md (LAM-14, LAM-27, LAM-32)
</read_first>

<acceptance_criteria>
1. `grep -n "getShareTag\|readShareTag" src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` returns zero matches (methods removed)
2. `grep -n "initializeClient" src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` returns zero matches (method removed)
3. `grep -n "keyShift" src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` returns zero matches (renamed to sneakKey)
4. `grep -n "sneakKey" src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` returns exactly 2 matches (lines 84 and 147)
5. `grep -n "getType()" src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` returns zero matches (renamed to getCategory)
6. `grep -n "getCategory()" src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` returns exactly 2 matches
7. `grep -n "getValue()" src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` returns zero matches (renamed to getCode)
8. `grep -n "getCode()" src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` returns exactly 2 matches
9. `grep -n "import net.minecraft.nbt.CompoundTag" src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` returns exactly 1 match
10. `grep -n "\.ifPresent(" src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` returns zero matches (LazyOptional patterns converted to null checks)
11. `grep -n "\.isPresent()" src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` returns zero matches
12. `grep -c "import" src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` returns at most the original count minus unused `IClientItemExtensions` (if no other consumer)
</acceptance_criteria>

<action>
Perform these edits in order:

1. **Remove `getShareTag` method** (lines 53-61):
   - Delete the entire method body including `@Nullable` annotation

2. **Remove `readShareTag` method** (lines 63-70):
   - Delete the entire method body including its annotation

3. **Remove `initializeClient` method** (lines 125-137):
   - Delete the entire method including `@Override`

4. **Remove unused import** (line 23):
   - Delete `import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;`
   - Also delete `import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;` (line 13) if no other references
   - Also delete `import java.util.function.Consumer;` (line 33) if no other references

5. **Add missing import** after line 3 (alphabetically):
   ```java
   import net.minecraft.nbt.CompoundTag;
   ```

6. **Rename `keyShift` → `sneakKey`** (line 84):
   - Replace `mc.options.keyShift` with `mc.options.sneakKey`
   - Also on line ~147 (inside `isShiftKeyDown()`): replace `Minecraft mc = Minecraft.getInstance(); KeyMapping shift = mc.options.keyShift;` with `sneakKey`

7. **Rename `getType()` → `getCategory()`** (line 150):
   - Replace `key.getType()` with `key.getCategory()`

8. **Rename `getValue()` → `getCode()`** (lines 151, 153):
   - Replace `key.getValue()` with `key.getCode()` (2 occurrences)

9. **Fix LazyOptional pattern** in `appendForgeEnergyInfo` (lines 82-98):
   - Current: `if (stack.getCapability(Capabilities.EnergyStorage.ITEM) instanceof FEBladeStorage energy) { ... }`
   - This pattern is OK — `instanceof` with null check. No change needed.

10. **Fix LazyOptional pattern** in `isDamageable` (lines 44-49):
    - Current: `if (stack.getCapability(Capabilities.EnergyStorage.ITEM) instanceof FEBladeStorage energy && energy.isEnergyDurability())`
    - Already OK — null-safe instanceof pattern.

11. **Fix LazyOptional pattern** on line 173-174 (in `onSlashBladeUpdate`):
    - Current: `living.getCapability(CapabilityConcentrationRank.RANK_POINT).ifPresent(cap->cap.addRankPoint(living, cap.getMaxCapacity()));`
    - Replace with:
    ```java
    var rank = living.getCapability(CapabilityConcentrationRank.RANK_POINT);
    if (rank != null) {
        rank.addRankPoint(living, rank.getMaxCapacity());
    }
    ```

12. **Re-verify the removed getShareTag/readShareTag**: After deletion, `ItemFEBlade` no longer references `BLADESTATE` in those methods. The remaining usage of `BLADESTATE` is in `isDamageable` super call and event handlers. No additional changes needed.

13. **Remove `import cn.mmf.energyblade.client.render.EnergyBladeBEWLR;`** (line 5):
    - Since `initializeClient` was removed, this import is no longer needed. Delete it.
</action>

---

### Task 2: ClientSetupHandler.java — EventBus Import Fix + enqueueWork + RegisterClientExtensionsEvent (CLI-04, CLI-05, CLI-02)

**Requirements:** CLI-02, CLI-04, CLI-05

**Impacted file:** `src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java`

<read_first>
- src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java
- src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java
- .planning/phases/06-client-systems/06-RESEARCH.md
- docs/migration/LOADER_API_MAP.md (LAM-15, LAM-17, LAM-18, LAM-22)
</read_first>

<acceptance_criteria>
1. `grep -n "import net.neoforged.fml.common.Mod;" src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java` returns zero matches
2. `grep -n "import net.neoforged.fml.common.EventBusSubscriber;" src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java` returns exactly 1 match
3. `grep -n "enqueueWork" src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java` returns exactly 1 match (inside `setModelUser`)
4. `grep -n "RegisterClientExtensionsEvent" src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java` returns exactly 2 matches (import + handler method)
5. `grep -n "EnergyBladeBEWLR" src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java` returns exactly 1 match
6. `grep -n "RegisterKeyMappingsEvent" src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java` returns exactly 2 matches (import + handler) — key registration preserved
7. `grep -n "ModifyBakingResult" src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java` returns exactly 2 matches (import + handler) — model baking preserved
</acceptance_criteria>

<action>
Perform these edits:

1. **Fix import on line 12:**
   - OLD: `import net.neoforged.fml.common.Mod;`
   - NEW: `import net.neoforged.fml.common.EventBusSubscriber;`

2. **Wrap `ItemProperties.register` in `enqueueWork()` (lines 22-31):**
   - Change `setModelUser` method body from:
   ```java
   public static void setModelUser(final FMLClientSetupEvent event) {
       Energyblade.ITEMS.getEntries().forEach(blade -> {
           if (blade.get() instanceof ItemSlashBlade) {
               ItemProperties.register(blade.get(), SlashBlade.prefix("user"), (stack, level, entity, seed) -> {
                   BladeModel.user = entity;
                   return 0;
               });
           }
       });
   }
   ```
   - To:
   ```java
   public static void setModelUser(final FMLClientSetupEvent event) {
       event.enqueueWork(() -> {
           Energyblade.ITEMS.getEntries().forEach(blade -> {
               if (blade.get() instanceof ItemSlashBlade) {
                   ItemProperties.register(blade.get(), SlashBlade.prefix("user"), (stack, level, entity, seed) -> {
                       BladeModel.user = entity;
                       return 0;
                   });
               }
           });
       });
   }
   ```

3. **Add `RegisterClientExtensionsEvent` handler** (insert after `registerKeyMapping` method, after line 36):
   - Add new import at top (after existing NeoForge client imports):
   ```java
   import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
   import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
   ```
   - Add import for BEWLR:
   ```java
   import cn.mmf.energyblade.client.render.EnergyBladeBEWLR;
   ```
   - If `Minecraft` not already imported, add:
   ```java
   import net.minecraft.client.Minecraft;
   ```
   - Add the handler method:
   ```java
   @SubscribeEvent
   public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
       Energyblade.ITEMS.getEntries().forEach(blade -> {
           if (blade.get() instanceof ItemSlashBlade) {
               event.registerItem(new IClientItemExtensions() {
                   private final EnergyBladeBEWLR renderer = new EnergyBladeBEWLR(
                       Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                       Minecraft.getInstance().getEntityModelLoader()
                   );
   
                   @Override
                   public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                       return renderer;
                   }
               }, blade.get());
           }
       });
   }
   ```
   - Note: `getEntityModelLoader()` (not `getEntityModels()`) matches the BEWLR constructor change from Wave 1 Task 4.

4. **Verify existing handlers are untouched:**
   - `registerKeyMapping` (lines 33-36): No changes needed — already uses `RegisterKeyMappingsEvent`
   - `baked` (lines 39-46): No changes needed — already uses `ModelEvent.ModifyBakingResult`
</action>

---

### Task 3: InputHandler.java — InputEvent.Key → ClientTickEvent.Post Migration (CLI-06)

**Requirements:** CLI-06

**Impacted file:** `src/main/java/cn/mmf/energyblade/client/InputHandler.java`

<read_first>
- src/main/java/cn/mmf/energyblade/client/InputHandler.java
- .planning/phases/06-client-systems/06-RESEARCH.md (section 5)
- docs/migration/LOADER_API_MAP.md (LAM-19, LAM-32)
</read_first>

<acceptance_criteria>
1. `grep -n "InputEvent" src/main/java/cn/mmf/energyblade/client/InputHandler.java` returns zero matches
2. `grep -n "ClientTickEvent" src/main/java/cn/mmf/energyblade/client/InputHandler.java` returns exactly 1 match (method parameter)
3. `grep -n "\.isDown()" src/main/java/cn/mmf/energyblade/client/InputHandler.java` returns zero matches
4. `grep -n "consumeClick" src/main/java/cn/mmf/energyblade/client/InputHandler.java` returns exactly 1 match
5. `grep -n "\.isPresent()" src/main/java/cn/mmf/energyblade/client/InputHandler.java` returns zero matches
6. `grep -n "import net.neoforged.fml.common.Mod;" src/main/java/cn/mmf/energyblade/client/InputHandler.java` returns zero matches (unused import removed)
7. `grep -n "NetworkPacketHandler" src/main/java/cn/mmf/energyblade/client/InputHandler.java` returns zero matches (unused import removed — only PacketDistributor is used)
</acceptance_criteria>

<action>
Perform these edits:

1. **Remove unused imports:**
   - Line 8: Delete `import cn.mmf.energyblade.NetworkPacketHandler;`
   - Line 18: Delete `import net.neoforged.fml.common.Mod;`

2. **Replace `InputEvent` import (line 19):**
   - OLD: `import net.neoforged.neoforge.client.event.InputEvent;`
   - NEW: `import net.neoforged.neoforge.client.event.ClientTickEvent;`

3. **Replace event handler method signature (line 33):**
   - Change method parameter type and annotation:
   - OLD:
   ```java
   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent()
   public static void onPlayerPostTick(InputEvent.Key event) {
   ```
   - NEW:
   ```java
   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent()
   public static void onClientTick(ClientTickEvent.Post event) {
   ```

4. **Replace key detection pattern (line 39):**
   - OLD: `if (player.getMainHandItem().isEmpty() || !player.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).isPresent())`
   - NEW: `if (player.getMainHandItem().isEmpty() || player.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE) == null)`

5. **Replace `isDown()` with `consumeClick()` (line 42):**
   - OLD:
   ```java
   if(InputHandler.KEY_CHARGE.isDown()) {
       PacketDistributor.sendToServer(new PowerSwitchPacket("triggered"));
   }
   ```
   - NEW:
   ```java
   while (KEY_CHARGE.consumeClick()) {
       PacketDistributor.sendToServer(new PowerSwitchPacket("triggered"));
   }
   ```

6. **Remaining existing code stays:**
   - `@EventBusSubscriber(value = Dist.CLIENT, modid = Energyblade.MODID)` (line 24) — correct; no `bus` param defaults to game bus, which ClientTickEvent requires
   - `KEY_CHARGE` constant definition (lines 27-29) — already uses `KeyConflictContext`, `KeyModifier`, `InputConstants.Type.KEYSYM` from NeoForge — no changes
</action>

---

### Task 4: EnergyBladeBEWLR.java — Constructor + LazyOptional Render Fixes (CLI-03)

**Requirements:** CLI-03

**Impacted file:** `src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java`

<read_first>
- src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java
- .planning/phases/06-client-systems/06-RESEARCH.md (sections 7, 9)
- .planning/phases/06-client-systems/06-VANILLA-RESEARCH.md (Q2, Q3)
- docs/migration/LOADER_API_MAP.md (LAM-27, LAM-32)
</read_first>

<acceptance_criteria>
1. `grep -n "EntityModelSet" src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java` returns zero matches
2. `grep -n "EntityModelLoader" src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java` returns exactly 1 match (constructor param)
3. `grep -n "import net.minecraft.client.model.geom.EntityModelSet" src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java` returns zero matches
4. `grep -n "import net.minecraft.client.model.geom.EntityModelLoader" src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java` returns exactly 1 match
5. `grep -n "\.filter(" src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java` returns zero matches (LazyOptional Optional chain removed)
6. `grep -n "\.map(" src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java` returns zero matches (LazyOptional Optional chain removed)
7. `grep -n "\.orElseGet(" src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java` returns zero matches
8. `grep -n "getCapability.*\.isPresent(" src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java` returns zero matches (LazyOptional `.isPresent()` removed; plain Optional `.isPresent()` on model/texture getters is fine)
9. `grep -n "getCapability.*BLADESTATE.*!= null" src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java` returns exactly 1 match
</acceptance_criteria>

<action>
Perform these edits:

1. **Replace import (line 12):**
   - OLD: `import net.minecraft.client.model.geom.EntityModelSet;`
   - NEW: `import net.minecraft.client.model.geom.EntityModelLoader;`

2. **Replace constructor signature (line 24):**
   - OLD: `public EnergyBladeBEWLR(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {`
   - NEW: `public EnergyBladeBEWLR(BlockEntityRenderDispatcher p_172550_, EntityModelLoader p_172551_) {`
   - Super call `super(p_172550_, p_172551_)` stays the same (just new type flows through)

3. **Fix LazyOptional pattern for modelLocation (lines 40-42):**
   - OLD:
   ```java
   ResourceLocation modelLocation = stack.getCapability(ItemSlashBlade.BLADESTATE)
           .filter(s -> s.getModel().isPresent()).map(s -> s.getModel().get())
           .orElseGet(() -> stackDefaultModel(stack));
   ```
   - NEW:
   ```java
   var bladeState = stack.getCapability(ItemSlashBlade.BLADESTATE);
   ResourceLocation modelLocation = (bladeState != null && bladeState.getModel().isPresent())
           ? bladeState.getModel().get()
           : stackDefaultModel(stack);
   ```

4. **Fix LazyOptional pattern for textureLocation (lines 44-46):**
   - OLD:
   ```java
   ResourceLocation textureLocation = stack.getCapability(ItemSlashBlade.BLADESTATE)
           .filter(s -> s.getTexture().isPresent()).map(s -> s.getTexture().get())
           .orElseGet(() -> stackDefaultTexture(stack));
   ```
   - NEW:
   ```java
   ResourceLocation textureLocation = (bladeState != null && bladeState.getTexture().isPresent())
           ? bladeState.getTexture().get()
           : stackDefaultTexture(stack);
   ```
   - Note: reuses `bladeState` variable from step 3 above.

5. **Verify remaining code unchanged:**
   - `renderIcon` method body after modelLocation/textureLocation stays unchanged
   - Energy storage `instanceof` pattern (line 33) is already null-safe — no change
</action>

---

### Task 5: JEICompat.java — LazyOptional Pattern Fixes (CLI-07)

**Requirements:** CLI-07

**Impacted file:** `src/main/java/cn/mmf/energyblade/compat/JEICompat.java`

<read_first>
- src/main/java/cn/mmf/energyblade/compat/JEICompat.java
- .planning/phases/06-client-systems/06-RESEARCH.md (section 12)
- docs/migration/LOADER_API_MAP.md (LAM-27, LAM-32)
</read_first>

<acceptance_criteria>
1. `grep -n "\.ifPresent(" src/main/java/cn/mmf/energyblade/compat/JEICompat.java` returns zero matches
2. `grep -n "\.map(" src/main/java/cn/mmf/energyblade/compat/JEICompat.java` returns zero matches
3. `grep -n "\.orElse(" src/main/java/cn/mmf/energyblade/compat/JEICompat.java` returns zero matches
4. `grep -n "!= null" src/main/java/cn/mmf/energyblade/compat/JEICompat.java` returns at least 1 match
5. `grep -n "getPluginUid" src/main/java/cn/mmf/energyblade/compat/JEICompat.java` returns exactly 1 match (preserved)
6. `grep -n "@JeiPlugin" src/main/java/cn/mmf/energyblade/compat/JEICompat.java` returns exactly 1 match (preserved)
</acceptance_criteria>

<action>
Perform these edits:

1. **Fix `registerItemSubtypes` method (lines 19-28):**
   Replace the entire method body:
   
   OLD:
   ```java
   @Override
   public void registerItemSubtypes(ISubtypeRegistration registration) {
       registration.registerSubtypeInterpreter(Energyblade.FORGE_ENERGY_BLADE.get(), (stack, context) -> {
           // 同步nbt到Cap
           stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(cap -> {
               cap.deserializeNBT(stack.getOrCreateTag().getCompound("bladeState"));
           });

           return stack.getCapability(ItemSlashBlade.BLADESTATE).map(cap -> cap.getTranslationKey()).orElse("");
       });
   }
   ```
   
   NEW:
   ```java
   @Override
   public void registerItemSubtypes(ISubtypeRegistration registration) {
       registration.registerSubtypeInterpreter(Energyblade.FORGE_ENERGY_BLADE.get(), (stack, context) -> {
           var bladeState = stack.getCapability(ItemSlashBlade.BLADESTATE);
           if (bladeState != null) {
               bladeState.deserializeNBT(stack.getOrCreateTag().getCompound("bladeState"));
               return bladeState.getTranslationKey();
           }
           return "";
       });
   }
   ```

2. **Preserve unchanged:**
   - `@JeiPlugin` annotation (line 10) — no change
   - `getPluginUid()` (line 14) — no change
   - imports — no change needed
</action>

---

## Wave 2 — BEWLR Registration Finalization (depends on Wave 1)

Depends on: Wave 1 Task 1 (initializeClient removed from ItemFEBlade), Wave 1 Task 4 (EnergyBladeBEWLR constructor fixed), Wave 1 Task 2 (ClientSetupHandler import fixed).

**NOTE:** The `RegisterClientExtensionsEvent` handler was already added in Wave 1 Task 2. Wave 2 is a validation/verification wave that ensures:
- The BEWLR constructor uses `getEntityModelLoader()` (verified against Task 4)
- `initializeClient` is removed from ItemFEBlade (verified against Task 1)
- The handler compiles and registers correctly

### Task 6: End-to-End Client Render Integration Verification (CLI-02, CLI-03)

**Requirements:** CLI-02, CLI-03

**Impacted files:** ClientSetupHandler.java, EnergyBladeBEWLR.java, ItemFEBlade.java (read-only verification)

<read_first>
- src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java
- src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java
- src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java
</read_first>

<acceptance_criteria>
1. `./gradlew compileJava` exits with code 0 and zero ERROR-level messages
2. ClientSetupHandler.java contains exactly one `RegisterClientExtensionsEvent` handler
3. The BEWLR is instantiated with `Minecraft.getInstance().getEntityModelLoader()` (not `getEntityModels()`)
4. `ItemFEBlade.java` has NO `initializeClient` method
5. No file in `src/main/java/cn/mmf/energyblade/` contains the string `getEntityModels()` (all migrated)
</acceptance_criteria>

<action>
1. Run `./gradlew compileJava` with timeout 300000ms
2. Verify the compile output contains zero errors
3. If errors exist:
   - Check that `getEntityModelLoader()` is used in both ClientSetupHandler.java and EnergyBladeBEWLR.java
   - Check that `EntityModelLoader` import exists in both files
   - Check that no stale `EntityModelSet` references remain
4. If compile succeeds: Wave 2 is complete
</action>

---

## Verification (Phase-Level)

After all waves complete, run these checks:

```bash
# Compile check
./gradlew compileJava

# Verify no Forge-only imports remain in client files
rg "import net\.minecraftforge\." src/main/java/cn/mmf/energyblade/client/ src/main/java/cn/mmf/energyblade/compat/ src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java
# Expected: zero matches

# Verify all NeoForge client imports are correct
rg "import net\.neoforged\.(neoforge|fml|bus|api)" src/main/java/cn/mmf/energyblade/client/ src/main/java/cn/mmf/energyblade/compat/ src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java

# Verify vanilla API renames are complete
rg "keyShift|getType\(\)|getValue\(\)|\.isPresent\(\)|\.ifPresent\(|\.filter\(|\.map\(|\.orElse\(" src/main/java/cn/mmf/energyblade/
# Expected: zero matches (or only false positives from vanilla Optional usage like s.getModel().isPresent())
```

## must_haves (Goal-Backward Verification)

These conditions MUST be true for the phase to be considered complete:

1. `./gradlew compileJava` succeeds with zero errors (all 7 requirements)
2. `ClientSetupHandler.java` imports `EventBusSubscriber` not `Mod` (CLI-04)
3. `ItemProperties.register` is wrapped in `event.enqueueWork()` (CLI-04)
4. `RegisterClientExtensionsEvent` handler exists in `ClientSetupHandler.java` (CLI-02)
5. `ItemFEBlade.java` has no `initializeClient`, `getShareTag`, or `readShareTag` methods (CLI-01, CLI-02)
6. `EnergyBladeBEWLR` constructor takes `(BlockEntityRenderDispatcher, EntityModelLoader)` (CLI-03)
7. `options.sneakKey` replaces `options.keyShift` in `ItemFEBlade.java` (CLI-01)
8. `getCategory()` and `getCode()` replace `getType()` and `getValue()` in `ItemFEBlade.java` (CLI-01)
9. `InputHandler.java` subscribes to `ClientTickEvent.Post` not `InputEvent.Key` (CLI-06)
10. `KEY_CHARGE.consumeClick()` replaces `KEY_CHARGE.isDown()` in `InputHandler.java` (CLI-06)
11. `JEICompat.java` uses null checks instead of `.ifPresent()` / `.map().orElse()` chains (CLI-07)
12. `RegisterKeyMappingsEvent` handler still exists and compiles (CLI-05)
13. `ModelEvent.ModifyBakingResult` handler still exists and compiles (CLI-04)
14. No `net.minecraftforge.*` imports in any modified file (CLI-01)

## Risk Register

| Risk | Impact | Mitigation |
|------|--------|------------|
| SlashBlade NeoForge port not available | Blocks CLI-02 (BEWLR registration), CLI-03 (BEWLR rendering), CLI-07 (JEI) | Verify SlashBlade availability before executing. If not available, mark as blocked and report. |
| JEI 1.21.1 NeoForge API changed `@JeiPlugin` annotation | Blocks CLI-07 | Research JEI NeoForge 1.21.1 changelog. Per research, `@JeiPlugin` likely unchanged but unconfirmed. |
| `CapabilityConcentrationRank.RANK_POINT` from SlashBlade uses old Forge capability API | Blocks CLI-01 LazyOptional fix in ItemFEBlade.java line 173 | If SlashBlade not ported, leave as compilation error for Phase 7 cleanup. |
| `ItemSlashBlade.BLADESTATE` capability pattern unknown in NeoForge | Affects CLI-03, CLI-06, CLI-07 | Assume the same `getCapability()` null-returning pattern; verify at compile time. |
