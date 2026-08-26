plugins {
	id("mod-platform")
	id("dev.kikugie.loom-back-compat")
}

stonecutter {
	val (version, loader) = current.project.split('-', limit = 2)
	properties.tags(version, loader)

	replacements.string(current.parsed >= "1.21.11") {
		replace("ResourceLocation", "Identifier")
		replace("location()", "identifier()")
	}
	replacements.string(current.parsed < "1.21.11") {
		replace("Identifier", "ResourceLocation")
		replace("identifier()", "location()")
	}
	replacements.string(current.parsed >= "26.1.2") {
		replace("FabricDataOutput", "FabricPackOutput")
	}
}

platform {
	loader = "fabric"
	dependencies {
		required("minecraft") {
			fabricLikeVersionRange = prop("deps.minecraft")
		}
		required("fabric-api") {
			slug("fabric-api")
			fabricLikeVersionRange = ">=${prop("deps.fabric-api")}"
		}
		required("fabricloader") {
			fabricLikeVersionRange = ">=${prop("deps.fabric-loader")}"
		}
		optional("modmenu") {}
	}
}

mixins {
	common {
		always(
			"accesor.LivingEntityRendererAccesor",
			"accesor.PlayerModelAccesor",
			"HumanoidArmorLayerMixin",
			"HumanoidModelMixin",
			"LeashAttachMixin",
			"LivingEntityMixin",
			"PlayerLeashStateMixin",
			"PlayerRendererMixin",
			"SkinCustomizationScreenMixin",
			"StuckInBodyLayerMixin"
		)
		minVersion("1.21.2","LivingEntityRenderStateMixin")
		minVersion("1.21.2","LivingEntityRendererMixin")
		minVersion("1.21.2","HumanoidRenderStateMixin")
	}
}

loom {
	accessWidenerPath = rootProject.file("src/main/resources/aw/${sc.current.version}.accesswidener")
	runs.named("client") {
		client()
		ideConfigGenerated(true)
		runDir = "run/"
		environment = "client"
		programArgs("--username=Dev")
		configName = "Fabric Client"
	}
	runs.named("server") {
		server()
		ideConfigGenerated(true)
		runDir = "run/"
		environment = "server"
		configName = "Fabric Server"
	}
}

fabricApi {
	configureDataGeneration {
		outputDirectory = file("${rootDir}/versions/datagen/${sc.current.version.split("-")[0]}/src/main/generated")
		client = true
	}
}

repositories {
	mavenCentral()
	strictMaven("https://maven.terraformersmc.com/", "com.terraformersmc") { name = "TerraformersMC" }
	strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }
}

configurations.all {
	resolutionStrategy {
		force("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")
	if (sc.current.parsed < "26") {
		mappings(loom.layered {
			officialMojangMappings()
			if (hasProperty("deps.parchment"))
				parchment("org.parchmentmc.data:parchment-${prop("deps.parchment")}@zip")
		})
	}
	modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
	// implementation(libs.moulberry.mixinconstraints)
	// include(libs.moulberry.mixinconstraints)
	modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
	modLocalRuntime("com.terraformersmc:modmenu:${prop("deps.modmenu")}")
	modImplementation("maven.modrinth:entity-model-features:${prop("deps.emf")}")
	modImplementation("maven.modrinth:entitytexturefeatures:${prop("deps.etf")}")
}
