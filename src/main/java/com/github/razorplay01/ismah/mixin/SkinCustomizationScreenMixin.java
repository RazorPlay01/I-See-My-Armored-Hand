package com.github.razorplay01.ismah.mixin;

import com.github.razorplay01.ismah.config.FirstPersonRenderConfig;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? >=1.21.1{
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SkinCustomizationScreen;
//?}else{
/*import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
*///?}

@Mixin(SkinCustomizationScreen.class)
public abstract class SkinCustomizationScreenMixin extends OptionsSubScreen {
    protected SkinCustomizationScreenMixin(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

	//? >=1.21.1{
    @Inject(method = "addOptions", at = @At("RETURN"))
    private void addFirstPersonRenderOptions(CallbackInfo ci) {
		//? >= 1.21.11 {
        this.list.addHeader(Component.translatable("options.ismah.first_person_layers"));
		//?}
        this.list.addSmall(
                FirstPersonRenderConfig.createArmorOption(),
                FirstPersonRenderConfig.createArrowsOption()
        );
        this.list.addSmall(FirstPersonRenderConfig.createLeashOption());
    }
	//?}else{
	/*@Inject(method = "init", at = @At("RETURN"))
	private void addFirstPersonRenderOptions(CallbackInfo ci) {
		// Calculate the next free row after the vanilla buttons
		// Vanilla places 7 model-part toggles + main-hand button = 8 widgets → 4 rows
		// Then the Done button is on the next row. We put our options after that.
		int row = 5; // 0-based row after the Done button

		this.addRenderableWidget(
				FirstPersonRenderConfig.createArmorOption()
						.createButton(this.options, this.width / 2 - 155, this.height / 6 + 24 * row, 150)
		);
		this.addRenderableWidget(
				FirstPersonRenderConfig.createArrowsOption()
						.createButton(this.options, this.width / 2 - 155 + 160, this.height / 6 + 24 * row, 150)
		);

		row++;
		this.addRenderableWidget(
				FirstPersonRenderConfig.createLeashOption()
						.createButton(this.options, this.width / 2 - 155, this.height / 6 + 24 * row, 150)
		);
	}
	*///?}
}
