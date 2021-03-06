package ua.leonidius.trdinterface.controllers.buy.items;

import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import me.onebone.economyapi.EconomyAPI;
import ua.leonidius.trdinterface.Message;
import ua.leonidius.trdinterface.Trading;
import ua.leonidius.trdinterface.controllers.AmountSelectorController;
import ua.leonidius.trdinterface.controllers.InfoController;
import ua.leonidius.trdinterface.models.BuyableItem;
import ua.leonidius.trdinterface.models.ShopItem;
import ua.leonidius.trdinterface.views.ScreenManager;
import ua.leonidius.trdinterface.views.screens.AmountSelectorScreen;

public class BuyAmountSelectorController extends AmountSelectorController {

    private final BuyableItem item;

    public BuyAmountSelectorController(ScreenManager manager, BuyableItem item) {
        super(manager);
        this.item = item;
    }

    @Override
    public void showScreen() {
        // Checking if there is enough place in inventory and money
        double price = item.getPrice();

        int maxByMoney = getMaxByMoney(price);
        int maxByInventory = getMaxByInventory();

        if (maxByMoney == 0 && maxByInventory == 0) {
            showInfoScreen(Message.WDW_VIEWING_BUYABLE_ITEM.getText(),
                    buildItemDescription()
                            + "\n\n" + Message.BUY_NO_SPACE_AND_MONEY.getText());
        } else if (maxByMoney == 0) {
            showInfoScreen(Message.WDW_VIEWING_BUYABLE_ITEM.getText(),
                    buildItemDescription()
                            + "\n\n" + Message.BUY_NO_MONEY.getText());
        } else if (maxByInventory == 0) {
            showInfoScreen(Message.WDW_VIEWING_BUYABLE_ITEM.getText(),
                    buildItemDescription()
                            + "\n\n" + Message.BUY_NO_SPACE.getText());
        } else {
            int maxAmount = Math.min(maxByInventory, maxByMoney);
            manager.addAndShow(new AmountSelectorScreen(this,
                    Message.WDW_BUY_TITLE.getText(item.toGameItem().getName()),
                    buildItemDescription(), maxAmount), true);
        }
    }

    @Override
    public void selectAmount(int amount) {
        double price = item.getPrice();
        double cost = amount * price;

        Item gameItem = item.toGameItem();
        gameItem.setCount(amount);

        // Check in case player has lost money between selecting an item
        // and confirming buying
        if (EconomyAPI.getInstance().myMoney(manager.getPlayer()) < cost) {
            showInfoScreen(Message.WDW_FAIL_TITLE.getText(),
                    Message.BUY_NO_MONEY.getText());
            return;
        }

        // Check in case something was added to the player's inventory
        // between selecting an item and confirming buying and now
        // there is not enough space
        if (!manager.getPlayer().getInventory().canAddItem(gameItem)) {
            showInfoScreen(Message.WDW_FAIL_TITLE.getText(),
                    Message.BUY_NO_SPACE.getText());
            return;
        }

        EconomyAPI.getInstance().reduceMoney(manager.getPlayer(), cost);
        manager.getPlayer().getInventory().addItem(gameItem);

        if (Trading.getSettings().logTransactions()) {
            Message.LOG_BOUGHT.log(manager.getPlayer().getName(),
                    amount, item.getName(), item.getItemId(),
                    cost, Trading.getSettings().getCurrency());
        }

        showInfoScreen(Message.WDW_SUCCESS_TITLE.getText(),
                Message.BUY_SUCCESS.getText(amount, item.getName(),
                        cost, Trading.getSettings().getCurrency()));
    }

    @Override
    protected ShopItem getItem() {
        return item;
    }

    private int getMaxByMoney(double priceWithDiscount) {
        double money = EconomyAPI.getInstance().myMoney(manager.getPlayer());
        return (int) Math.floor(money / priceWithDiscount);
    }

    private int getMaxByInventory() {
        PlayerInventory inventory = manager.getPlayer().getInventory();
        Item gameItem = item.toGameItem();
        int maxStack = gameItem.getMaxStackSize();
        int amount = 0;
        for (int i = 0; i < 36; i++) {
            Item itemInSlot = inventory.getItem(i);
            if (itemInSlot.getId() == Item.AIR) {
                amount += maxStack;
            } else if (itemInSlot.equals(gameItem, true, true)) {
                amount += (maxStack - itemInSlot.getCount());
            }
        }
        return amount;
    }

    private void showInfoScreen(String title, String message) {
        new InfoController(manager, title, message).showScreen();
    }

}
