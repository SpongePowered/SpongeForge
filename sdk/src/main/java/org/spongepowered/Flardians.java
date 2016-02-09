package org.spongepowered;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.entity.TradeOfferData;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.type.Careers;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.List;
import java.util.Random;

@Plugin(id = "flardians", name = "Flardians", version = "0.1")
public class Flardians {

    public static final ItemType[] SELL_TYPES = new ItemType[] {ItemTypes.SLIME_BALL, ItemTypes.HARDENED_CLAY, ItemTypes.BLAZE_ROD, ItemTypes.APPLE,
                                                                ItemTypes.GHAST_TEAR, ItemTypes.COBBLESTONE, ItemTypes.STICK, ItemTypes.EMERALD,};
    public static final List<ItemType> BUYING_TYPES = ImmutableList.of(ItemTypes.ACACIA_DOOR, ItemTypes.LEAVES2, ItemTypes.BOOKSHELF, ItemTypes.COAL,
                                                                      ItemTypes.COBBLESTONE, ItemTypes.ANVIL, ItemTypes.IRON_ORE, ItemTypes.APPLE,
                                                                      ItemTypes.WHEAT_SEEDS, ItemTypes.DIRT);
    public static final Text FLARDARIAN = Text.of(TextColors.DARK_AQUA, TextStyles.BOLD, TextStyles.ITALIC, "Flardarian");
    public static final Text ITEM_DISPLAY = Text.of(TextColors.YELLOW, TextStyles.BOLD, "[", TextColors.GREEN, TextStyles.ITALIC, "FLARD",
                                                     TextStyles.RESET, TextColors.YELLOW, TextStyles.BOLD, "]");
    public static final Text LORE_FIRST = Text.of(TextColors.BLUE, TextStyles.ITALIC, "This is indeed a glorious day!");
    public static final Text LORE_SECOND = Text.of(TextColors.BLUE, TextStyles.ITALIC, "Shining sun makes the clouds flee");
    public static final Text LORE_THIRD = Text.of(TextColors.BLUE, TextStyles.ITALIC, "With State of ", TextColors.YELLOW, "Sponge",
                                                   TextColors.BLUE, " again today");
    public static final Text LORE_FOURTH = Text.of(TextColors.BLUE, TextStyles.ITALIC, "Granting delights for you and me");
    public static final Text LORE_FIFTH = Text.of(TextColors.BLUE, TextStyles.ITALIC, "For ", TextColors.YELLOW, "Sponge", TextColors.BLUE,
                                                   " is in a State of play");
    public static final Text LORE_SIXTH = Text.of(TextColors.BLUE, TextStyles.ITALIC, "Today, be happy as can be!");
    public static final Random RANDOM = new Random();

    @Inject private Game game;

    @Listener
    public void onSpawn(SpawnEntityEvent event) {
        for (final Entity entity : event.getEntities()) {
            if (entity.getType() == EntityTypes.VILLAGER && Math.random() > 0.7) {
                entity.offer(Keys.CAREER, Careers.CLERIC);
                entity.offer(Keys.DISPLAY_NAME, FLARDARIAN);
                entity.offer(Keys.SHOWS_DISPLAY_NAME, true);
                entity.offer(Keys.INVULNERABILITY, 10000);
                entity.offer(generateTradeOffer());
            }
        }
    }

    private TradeOfferData generateTradeOffer() {
        final int rand = RANDOM.nextInt(7);
        final int itemRand = RANDOM.nextInt(BUYING_TYPES.size());

        final DisplayNameData itemName = this.game.getDataManager().getManipulatorBuilder(DisplayNameData.class).get().create();
        itemName.set(Keys.DISPLAY_NAME, ITEM_DISPLAY);

        // Set up the lore data.
        final LoreData loreData = this.game.getDataManager().getManipulatorBuilder(LoreData.class).get().create();
        final ListValue<Text> lore = loreData.lore();
        lore.addAll(ImmutableList.of(LORE_FIRST, LORE_SECOND, LORE_THIRD, LORE_FOURTH, LORE_FIFTH, LORE_SIXTH));
        loreData.set(lore);

        // Create the selling item
        final ItemStack selling = this.game.getRegistry().createBuilder(ItemStack.Builder.class)
            .itemType(BUYING_TYPES.get(itemRand))
            .itemData(itemName)
            .itemData(loreData)
            .quantity(1)
            .build();

        // Create the buying item
        final ItemStack buying = this.game.getRegistry().createBuilder(ItemStack.Builder.class)
            .itemType(SELL_TYPES[rand])
            .quantity(1)
            .build();

        final TradeOffer.Builder builder = this.game.getRegistry().createBuilder(TradeOffer.Builder.class);

        final TradeOfferData tradeOfferData = this.game.getDataManager().getManipulatorBuilder(TradeOfferData.class).get().create();
        tradeOfferData.set(tradeOfferData.tradeOffers().add(builder.firstBuyingItem(buying).maxUses(10000).sellingItem(selling).build()));
        return tradeOfferData;
    }

}
