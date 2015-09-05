package io.github.hsyyid.payday;

import java.io.File;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.option.OptionSubject;
import org.spongepowered.api.service.scheduler.SchedulerService;
import org.spongepowered.api.service.scheduler.TaskBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

import com.erigitic.config.AccountManager;
import com.erigitic.main.TotalEconomy;
import com.google.inject.Inject;

@Plugin(id = "PayDay", name = "PayDay", version = "0.2", dependencies = "required-after:TotalEconomy")
public class Main
{
	public static Game game = null;

	@Inject
	private Logger logger;

	public Logger getLogger()
	{
		return logger;
	}

	@Inject
	@DefaultConfig(sharedRoot = true)
	private File dConfig;

	@Inject
	@DefaultConfig(sharedRoot = true)
	private ConfigurationLoader<CommentedConfigurationNode> confManager;

	@Listener
	public void onServerStart(GameStartedServerEvent event)
	{
		getLogger().info("PayDay loading...");

		game = event.getGame();

		SchedulerService scheduler = game.getScheduler();
		TaskBuilder taskBuilder = scheduler.createTaskBuilder();

		taskBuilder.execute(new Runnable()
		{
			public void run()
			{
				for(Player player : game.getServer().getOnlinePlayers())
				{
					Subject subject = player.getContainingCollection().get(player.getIdentifier());

					if (subject instanceof OptionSubject)
					{
						OptionSubject optionSubject = (OptionSubject) subject;
						double pay = Double.parseDouble(optionSubject.getOption("pay").or(""));

						player.sendMessage(Texts.of(TextColors.GOLD, "[PayDay]: ", TextColors.GRAY, "It's PayDay! Here is your salary of " + pay + " dollars! Enjoy!"));

						TotalEconomy totalEconomy = (TotalEconomy) game.getPluginManager().getPlugin("TotalEconomy").get().getInstance();
						AccountManager accountManager = totalEconomy.getAccountManager();
						BigDecimal amount = new BigDecimal(pay);
						accountManager.addToBalance(player.getUniqueId(), amount, true);
					}
				}
			}
		}).interval(1, TimeUnit.HOURS).name("PayDay - Pay").submit(game.getPluginManager().getPlugin("PayDay").get().getInstance());

		getLogger().info("-----------------------------");
		getLogger().info("PayDay was made by HassanS6000!");
		getLogger().info("Please post all errors on the Sponge Thread or on GitHub!");
		getLogger().info("Have fun, and enjoy! :D");
		getLogger().info("-----------------------------");
		getLogger().info("PayDay loaded!");
	}

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event)
	{
		Player player = event.getTargetEntity();

		Subject subject = player.getContainingCollection().get(player.getIdentifier());

		if (subject instanceof OptionSubject)
		{
			OptionSubject optionSubject = (OptionSubject) subject;
			double pay = Double.parseDouble(optionSubject.getOption("startingbalance").or(""));
			TotalEconomy totalEconomy = (TotalEconomy) game.getPluginManager().getPlugin("TotalEconomy").get().getInstance();
			AccountManager accountManager = totalEconomy.getAccountManager();

			if(!(accountManager.hasAccount(player.getUniqueId())))
			{
				player.sendMessage(Texts.of(TextColors.GOLD, "[PayDay]: ", TextColors.GRAY, "Welcome to the server! Here is " + pay + " dollars! Enjoy!"));
				BigDecimal amount = new BigDecimal(pay);
				accountManager.addToBalance(player.getUniqueId(), amount, true);
			}
		}
	}
}