package org.spongepowered;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.manipulators.DisplayNameData;
import org.spongepowered.api.data.manipulators.entities.CareerData;
import org.spongepowered.api.data.manipulators.entities.InvulnerabilityData;
import org.spongepowered.api.data.manipulators.entities.TradeOfferData;
import org.spongepowered.api.data.manipulators.items.LoreData;
import org.spongepowered.api.data.types.Careers;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.EntitySpawnEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.merchant.TradeOfferBuilder;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.List;
import java.util.Random;

@Plugin(id = "flardians", name = "Flardians", version = "0.1")
public class Flardians {

    @Inject private Game game;

    @Subscribe
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntity().getType() == EntityTypes.VILLAGER && Math.random() > 0.7) {

            Entity entity = event.getEntity();
            CareerData careerData = entity.getOrCreate(CareerData.class).get();
            careerData.setCareer(Careers.CLERIC);
            entity.offer(careerData);

            DisplayNameData displayNameData = entity.getOrCreate(DisplayNameData.class).get();
            displayNameData.setDisplayName(Texts.of(TextColors.DARK_AQUA, TextStyles.BOLD, TextStyles.ITALIC, "Flardarian"));
            displayNameData.setCustomNameVisible(true);
            entity.offer(displayNameData);

            InvulnerabilityData invulnerabilityData = game.getRegistry().getManipulatorRegistry().getBuilder(InvulnerabilityData.class).get().create();
            invulnerabilityData.setInvulnerableTicks(100000000);
            entity.offer(invulnerabilityData);

            ItemType[] randoms = new ItemType[] {ItemTypes.SLIME_BALL, ItemTypes.HARDENED_CLAY, ItemTypes.BLAZE_ROD, ItemTypes.GHAST_TEAR,
                    ItemTypes.COBBLESTONE, ItemTypes.STICK, ItemTypes.EMERALD, ItemTypes.APPLE};
            final int rand = new Random().nextInt(7);
            List<ItemType> itemTypes = ImmutableList.of(ItemTypes.ACACIA_DOOR, ItemTypes.LEAVES2, ItemTypes.BOOKSHELF, ItemTypes.COBBLESTONE,
                    ItemTypes.ANVIL, ItemTypes.IRON_ORE, ItemTypes.COAL, ItemTypes.APPLE, ItemTypes.WHEAT_SEEDS, ItemTypes.DIRT);
            final int itemRand = new Random().nextInt(itemTypes.size());

            DisplayNameData itemName = game.getRegistry().getManipulatorRegistry().getBuilder(DisplayNameData.class).get().create();
            itemName.setDisplayName(Texts.of(TextColors.YELLOW, TextStyles.BOLD, "[",
                    TextColors.GREEN, TextStyles.ITALIC, "FLARD", TextStyles.RESET, TextColors.YELLOW, TextStyles.BOLD, "]"));
            LoreData loreData = game.getRegistry().getManipulatorRegistry().getBuilder(LoreData.class).get().create();
            loreData.add(Texts.of(TextColors.BLUE, TextStyles.ITALIC, "This is indeed a glorious day!"));
            loreData.add(Texts.of(TextColors.BLUE, TextStyles.ITALIC, "Shining sun makes the clouds flee"));
            loreData.add(Texts.of(TextColors.BLUE, TextStyles.ITALIC, "With State of ", TextColors.YELLOW, "Sponge", TextColors.BLUE, " again "
                    + "today"));
            loreData.add(Texts.of(TextColors.BLUE, TextStyles.ITALIC, "Granting delights for you and me"));
            loreData.add(Texts.of(TextColors.BLUE, TextStyles.ITALIC, "For ", TextColors.YELLOW, "Sponge", TextColors.BLUE, " is in a State of play"));
            loreData.add(Texts.of(TextColors.BLUE, TextStyles.ITALIC, "Today, be happy as can be!"));

            ItemStack selling = game.getRegistry().getItemBuilder().itemType(itemTypes.get(itemRand))
                    .itemData(itemName).itemData(loreData).quantity(1).build();

            ItemStack buying = game.getRegistry().getItemBuilder().itemType(randoms[rand]).quantity(1).build();

            TradeOfferBuilder builder = game.getRegistry().getTradeOfferBuilder();

            TradeOfferData tradeOfferData = game.getRegistry().getManipulatorRegistry().getBuilder(TradeOfferData.class).get().create();
            tradeOfferData.addOffer(builder.firstBuyingItem(buying).maxUses(10000).sellingItem(selling).build());

            entity.offer(tradeOfferData);
        }
    }

}
