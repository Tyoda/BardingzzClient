package org.tyoda.wurm.bardingzzclient;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.Versioned;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * This mod allows server mods to load custom horse bardings for clients.
 * Read the readme on the <a href="https://github.com/Tyoda/BardingzzClient">GitHub page</a> for more info
 */
public class BardingzzClient implements WurmClientMod, PreInitable, Configurable, Versioned {
    private static final Logger logger =  Logger.getLogger(BardingzzClient.class.getName());
    public static final String version = "1.0";

    /**
     * The required prefix for model names to be recognized
     * by this mod.
     * This should never change for compatibility reasons.
     */
    public static final String requiredPrefix = "mod.bardingzz.";

    /**
     * Whether to suppress warning messages for missing models.
     * This is required because it starts looking for them before serverpacks
     * has loaded the modded models.
     * This only affects models that have names starting with {@value requiredPrefix}
     */
    private boolean suppressNoMapping = true;

    @Override
    public void configure(Properties p){
        suppressNoMapping = Boolean.parseBoolean(p.getProperty("suppressNoMapping", String.valueOf(suppressNoMapping)));
        logger.info("suppressNoMapping: "+suppressNoMapping);
    }

    @Override
    public void preInit(){
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();
            CtClass ctMountItems = classPool.getCtClass("com.wurmonline.client.renderer.cell.MountItems");
            CtClass ctConn = classPool.getCtClass("com.wurmonline.client.comm.ServerConnectionListenerClass");

            logger.info("Adding overload for ... ");
            logger.info("... changeBarding ...");
            CtMethod changeBardingModded = CtMethod.make("" +
                "public void changeBarding(boolean hasBarding, byte material, String itemModelName){" +
                    "this.changeBarding(hasBarding, material);" +
                    "this.bardingTextureOverrideName = itemModelName + \"riding.\";" +
                "}",
                ctMountItems
            );
            ctMountItems.addMethod(changeBardingModded);
            logger.info("... success ...");

            logger.info("... addBarding ...");
            CtMethod addBardingOverload = CtMethod.make("" +
                "public void addBarding(String itemModelName, long wurmId, byte material) {" +
                    "com.wurmonline.client.renderer.cell.CreatureCellRenderable sourceCreature = (com.wurmonline.client.renderer.cell.CreatureCellRenderable)this.creatures.get(new Long(wurmId));" +
                    "if (sourceCreature != null) {" +
                        "if (sourceCreature.getMountItems() == null) {" +
                            "sourceCreature.setMountItems(new com.wurmonline.client.renderer.cell.MountItems(sourceCreature));" +
                        "}" +
                        "if(itemModelName.startsWith(\""+requiredPrefix+"\")){" +
                            "sourceCreature.getMountItems().changeBarding(true, material, itemModelName);" +
                        "}else{" +
                            "sourceCreature.getMountItems().changeBarding(true, material);" +
                        "}" +
                    "}" +
                "}",
                ctConn
            );
            ctConn.addMethod(addBardingOverload);
            logger.info("... success");

            logger.info("Instrumenting addMountItem ...");
            CtMethod addMountItem = ctConn.getDeclaredMethod("addMountItem");
            addMountItem.instrument(new ExprEditor(){
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if(m.getMethodName().equals("addBarding")){
                        m.replace("addBarding(itemModelName, $$);");
                    }
                }
            });
            logger.info("... success");

            if(suppressNoMapping){
                logger.info("Suppressing no mapping messages ...");
                logger.info("... in getModelImpl ...");
                CtClass ctModelResource = classPool.getCtClass("com.wurmonline.client.renderer.model.ModelResourceLoader");
                CtMethod getModelImpl = ctModelResource.getDeclaredMethod("getModelImpl");
                getModelImpl.instrument(new ExprEditor(){
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if(m.getMethodName().equals("println")){
                            m.replace("if(!(code.startsWith(\""+requiredPrefix+"\"))){ System.out.println($1); }");
                            logger.info("... success ...");
                        }
                    }
                });

                logger.info("... in getInternalTexture ...");
                CtClass ctResourceTexture = classPool.getCtClass("com.wurmonline.client.resources.textures.ResourceTextureLoader");
                CtMethod getInternalTexture = ctResourceTexture.getDeclaredMethod("getInternalTexture",
                    new CtClass[]{
                            CtClass.booleanType,
                            classPool.getCtClass("java.lang.String"),
                            classPool.getCtClass("com.wurmonline.client.resources.textures.TextureLoader").getNestedClasses()[1],
                            CtClass.booleanType,
                            CtClass.booleanType,
                            CtClass.booleanType
                    }
                );
                getInternalTexture.instrument(new ExprEditor(){
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if(m.getMethodName().equals("println")){
                            m.replace("if(!(resourceName.startsWith(\""+requiredPrefix+"\"))){ System.out.println($1); }");
                            logger.info("... success");
                        }
                    }
                });
            }
        } catch (NotFoundException | CannotCompileException e){
            throw new HookException(e);
        }
    }

    @Override
    public String getVersion() {
        return version;
    }
}