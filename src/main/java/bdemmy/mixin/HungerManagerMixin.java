package bdemmy.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
	@Shadow
	private int foodLevel = 20;
	@Shadow
	private float foodSaturationLevel = 5.0F;
	@Shadow
	private float exhaustion;
	@Shadow
	private int foodTickTimer;
	@Shadow
	private int prevFoodLevel = 20;

	@Overwrite
	public void update(PlayerEntity player) {
		Difficulty difficulty = player.world.getDifficulty();
		this.prevFoodLevel = this.foodLevel;
		if (this.exhaustion > 4.0F) {
			this.exhaustion -= 4.0F;
			if (this.foodSaturationLevel > 0.0F) {
				this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
			} else if (difficulty != Difficulty.PEACEFUL) {
				this.foodLevel = Math.max(this.foodLevel - 1, 0);
			}
		}

		boolean bl = player.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION);
		if (bl && this.foodSaturationLevel > 0.0F && player.canFoodHeal() && this.foodLevel >= 20) {
			++this.foodTickTimer;
			if (this.foodTickTimer >= 10) {
				float f = Math.min(this.foodSaturationLevel, 2.0F);
				player.heal(f / 2.0F);
				this.addExhaustion(f);
				this.foodTickTimer = 0;
			}
		} else if (bl && this.foodLevel >= 8 && player.canFoodHeal()) {
			++this.foodTickTimer;
			if (this.foodTickTimer >= 80) {
				player.heal(1.0F);
				this.addExhaustion(2.0F);
				this.foodTickTimer = 0;
			}
		} else if (this.foodLevel <= 0) {
			++this.foodTickTimer;
			if (this.foodTickTimer >= 80) {
				if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
					player.damage(DamageSource.STARVE, 1.0F);
				}

				this.foodTickTimer = 0;
			}
		} else {
			this.foodTickTimer = 0;
		}
	}

	@Shadow
	public int getFoodLevel() {
		return 0;
	}

	@Shadow
	public int getPrevFoodLevel() {
		return 0;
	}

	@Shadow
	public boolean isNotFull() {
		return true;
	}

	@Shadow
	public void addExhaustion(float exhaustion) {}

	@Shadow
	public float getExhaustion() { return 0.0f; }

	@Shadow
	public float getSaturationLevel() { return 0.0f; }

	@Shadow
	public void setFoodLevel(int foodLevel) {}

	@Shadow
	public void setSaturationLevel(float saturationLevel) {}

	@Shadow
	public void setExhaustion(float exhaustion) {}
}
