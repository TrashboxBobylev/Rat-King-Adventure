package com.zrp200.rkpd2.items.quest;

import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.potions.PotionOfStrength;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class RedCrystal extends Item {
    {
        image = ItemSpriteSheet.RED_CRYSTAL;
        stackable = true;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return new ItemSprite.Glowing();
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    public static class StrengthRecipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {

        {
            inputs =  new Class[]{RedCrystal.class};
            inQuantity = new int[]{3};

            cost = 0;

            output = PotionOfStrength.class;
            outQuantity = 1;
        }

    }

}
