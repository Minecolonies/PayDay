package io.github.hsyyid.payday;

import com.google.inject.Inject;
import io.github.hsyyid.payday.utils.Utils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.option.OptionSubject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@Plugin(id = "PayDay", name = "PayDay", version = "0.4")
public class PayDay
{
	public static ConfigurationNode config;
	public static ConfigurationLoader<CommentedConfigurationNode> configurationManager;
	public static Game game;
	public static EconomyService economyService;

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
	public void onGameInit(GameInitializationEvent event)
	{
		getLogger().info("PayDay loading...");

		try
		{
			if (!dConfig.exists())
			{
				dConfig.createNewFile();
				config = confManager.load();
				confManager.save(config);
			}

			configurationManager = confManager;
			config = confManager.load();
		}
		catch (IOException exception)
		{
			getLogger().error("The default configuration could not be loaded or created!");
		}

		game = Sponge.getGame();

		Scheduler scheduler = game.getScheduler();
		Task.Builder taskBuilder = scheduler.createTaskBuilder();

		taskBuilder.execute(new Runnable()
		{
			public void run()
			{
				for (Player player : game.getServer().getOnlinePlayers())
				{
					Subject subject = player.getContainingCollection().get(player.getIdentifier());

					if (subject instanceof OptionSubject)
					{
						OptionSubject optionSubject = (OptionSubject) subject;

						if (optionSubject.getOption("pay").isPresent())
						{
							BigDecimal pay = new BigDecimal(Double.parseDouble(optionSubject.getOption("pay").get()));
							player.sendMessage(Text.of(TextColors.GOLD, "[PayDay]: ", TextColors.GRAY, "It's PayDay! Here is your salary of " + pay + " dollars! Enjoy!"));
							UniqueAccount uniqueAccount = null;

							if (economyService.getAccount(player.getUniqueId()).isPresent())
							{
								uniqueAccount = economyService.getAccount(player.getUniqueId()).get();
							}
							else
							{
								uniqueAccount = economyService.createAccount(player.getUniqueId()).get();
							}

							uniqueAccount.deposit(economyService.getDefaultCurrency(), pay, Cause.of(Sponge.getPluginManager().getPlugin("PayDay").get()));
						}
					}
				}
			}
		}).interval(1, Utils.getTimeUnit()).name("PayDay - Pay").submit(game.getPluginManager().getPlugin("PayDay").get().getInstance().get());

		getLogger().info("-----------------------------");
		getLogger().info("PayDay was made by HassanS6000!");
		getLogger().info("Please post all errors on the Sponge Thread or on GitHub!");
		getLogger().info("Have fun, and enjoy! :D");
		getLogger().info("-----------------------------");
		getLogger().info("PayDay loaded!");
	}
	
	@Listener
	public void onGamePostInit(GamePostInitializationEvent event)
	{
		Optional<EconomyService> econService = Sponge.getServiceManager().provide(EconomyService.class);
		
		if(econService.isPresent())
		{
			economyService = econService.get();
		}
		else
		{
			getLogger().error("Error! There is no Economy plugin found on this server, PayDay will not work correctly!");
		}
	}
		
	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event)
	{
		Player player = event.getTargetEntity();

		Subject subject = player.getContainingCollection().get(player.getIdentifier());

		if (subject instanceof OptionSubject)
		{
			OptionSubject optionSubject = (OptionSubject) subject;

			if (optionSubject.getOption("startingbalance").isPresent())
			{
				BigDecimal pay = new BigDecimal(Double.parseDouble(optionSubject.getOption("startingbalance").get()));
				player.sendMessage(Text.of(TextColors.GOLD, "[PayDay]: ", TextColors.GRAY, "Welcome to the server! Here is " + pay + " dollars! Enjoy!"));
				UniqueAccount uniqueAccount = null;

				if (economyService.getAccount(player.getUniqueId()).isPresent())
				{
					uniqueAccount = economyService.getAccount(player.getUniqueId()).get();
				}
				else
				{
					uniqueAccount = economyService.createAccount(player.getUniqueId()).get();
				}

				uniqueAccount.deposit(economyService.getDefaultCurrency(), pay, Cause.of(Sponge.getPluginManager().getPlugin("PayDay").get()));
			}
		}
	}

	public static ConfigurationLoader<CommentedConfigurationNode> getConfigManager()
	{
		return configurationManager;
	}
}
