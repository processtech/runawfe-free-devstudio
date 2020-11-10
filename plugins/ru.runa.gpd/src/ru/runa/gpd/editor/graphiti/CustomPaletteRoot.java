/*******************************************************************************
 * <copyright>
 *
 * Copyright (c) 2005, 2015 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API, implementation and documentation
 *    mwenz - Bug 341224: Allow to hide the selection and marquee tools in the palette
 *    Bug 336488 - DiagramEditor API
 *    fvelasco - Bug 396247 - ImageDescriptor changes
 *    mwenz - Bug 428068 - Automatically unselect a tool entry in palette like 'connection creation' after execution
 *    mwenz - Bug 467476 - NullPointerException in GFPaletteRoot.createModelIndependentTools
 *
 * </copyright>
 *
 *******************************************************************************/
package ru.runa.gpd.editor.graphiti;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.PaletteStack;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.palette.IConnectionCreationToolEntry;
import org.eclipse.graphiti.palette.ICreationToolEntry;
import org.eclipse.graphiti.palette.IObjectCreationToolEntry;
import org.eclipse.graphiti.palette.IPaletteCompartmentEntry;
import org.eclipse.graphiti.palette.IPaletteSeparatorEntry;
import org.eclipse.graphiti.palette.IStackToolEntry;
import org.eclipse.graphiti.palette.IToolEntry;
import org.eclipse.graphiti.tb.IToolBehaviorProvider;
import org.eclipse.graphiti.ui.editor.IEclipseImageDescriptor;
import org.eclipse.graphiti.ui.internal.Messages;
import org.eclipse.graphiti.ui.internal.editor.GFCreationTool;
import org.eclipse.graphiti.ui.internal.editor.GFMarqueeToolEntry;
import org.eclipse.graphiti.ui.internal.editor.GFPanningSelectionToolEntry;
import org.eclipse.graphiti.ui.internal.util.gef.MultiCreationFactory;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.jface.resource.ImageDescriptor;

//
// It is a copy of the class org.eclipse.graphiti.ui.internal.editor.GFPaletteRoot with some changes.
// The line:
//     pe.setToolClass(GFConnectionCreationTool.class);
// was changed to:
//     pe.setToolClass(CustomConnectionCreationTool.class);
//
/**
 * This PaletteRoot provides some standard-tools and adds them to the Palette.
 * <p>
 * Some of the standard-tools are: selection-tool, marque-tool, connection-tool,
 * creation-tools.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CustomPaletteRoot extends PaletteRoot {

    /**
     * later we can make this configurable in the toolbehaviour provider, so
     * that the dtp developer can influence the creation style from palette.<br>
     * If true then drag&drop from the palette is possible
     */
    private static boolean DND_FROM_PALETTE = true; //

    private IDiagramTypeProvider diagramTypeProvider;

    /**
     * Creates a new GFPaletteRoot for the diagram identified by the given
     * {@link IDiagramTypeProvider}. It is constructed by calling
     * createModelIndependentTools() and createCreationTools().
     * 
     * @param diagramTypeProvider
     *            the diagram type provider
     */
    public CustomPaletteRoot(IDiagramTypeProvider diagramTypeProvider) {
        super();
        if (diagramTypeProvider == null) {
            throw new IllegalArgumentException("diagramTypeProvider must not be null");
        }
        this.diagramTypeProvider = diagramTypeProvider;
        updatePaletteEntries();
    }

    /**
     * Creates resp. updates the PaletteEntries. All old PaletteEntries will be
     * removed and new ones will be created by calling the corresponding
     * create-methods.
     */
    public void updatePaletteEntries() {
        // remove old entries
        setDefaultEntry(null);
        @SuppressWarnings("unchecked")
        List<PaletteEntry> allEntries = new ArrayList<PaletteEntry>(getChildren());
        // MUST make a copy
        for (Iterator<PaletteEntry> iter = allEntries.iterator(); iter.hasNext();) {
            PaletteEntry entry = iter.next();
            remove(entry);
        }

        // create new entries
        add(createModelIndependentTools());

        IToolBehaviorProvider currentToolBehaviorProvider = diagramTypeProvider.getCurrentToolBehaviorProvider();

        IPaletteCompartmentEntry[] paletteCompartments = currentToolBehaviorProvider.getPalette();

        for (IPaletteCompartmentEntry compartmentEntry : paletteCompartments) {
            PaletteDrawer drawer = new PaletteDrawer(compartmentEntry.getLabel(), getImageDescriptor(compartmentEntry));
            if (!compartmentEntry.isInitiallyOpen()) {
                drawer.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
            }
            add(drawer);

            List<IToolEntry> toolEntries = compartmentEntry.getToolEntries();

            for (IToolEntry toolEntry : toolEntries) {

                if (toolEntry instanceof ICreationToolEntry) {
                    ICreationToolEntry creationToolEntry = (ICreationToolEntry) toolEntry;

                    PaletteEntry createTool = createTool(creationToolEntry);
                    if (createTool != null) {
                        drawer.add(createTool);
                    }
                } else if (toolEntry instanceof IStackToolEntry) {
                    IStackToolEntry stackToolEntry = (IStackToolEntry) toolEntry;
                    PaletteStack stack = new PaletteStack(stackToolEntry.getLabel(), stackToolEntry.getDescription(),
                            GraphitiUi.getImageService().getImageDescriptorForId(diagramTypeProvider.getProviderId(),
                                    stackToolEntry.getIconId()));
                    drawer.add(stack);
                    List<ICreationToolEntry> creationToolEntries = stackToolEntry.getCreationToolEntries();
                    for (ICreationToolEntry creationToolEntry : creationToolEntries) {
                        PaletteEntry createTool = createTool(creationToolEntry);
                        if (createTool != null) {
                            stack.add(createTool);
                        }
                    }
                } else if (toolEntry instanceof IPaletteSeparatorEntry) {
                    drawer.add(new PaletteSeparator());
                }
            }
        }
    }

    /**
     * Creates and adds the model-independent tools to a new PaletteContainer.
     * Those are the selection-tool and the marquee-tool. Both tools are only
     * added in case the methods
     * {@link IToolBehaviorProvider#isShowSelectionTool()} respectively
     * {@link IToolBehaviorProvider#isShowMarqueeTool()} allow it. The selection
     * tool will be set as the default tool in case it is added.
     * 
     * @return The PaletteContainer with the model-independent tools.
     */
    protected PaletteContainer createModelIndependentTools() {
        PaletteGroup controlGroup = new PaletteGroup(Messages.GraphicsPaletteRoot_0_xmen);
        List<PaletteEntry> entries = new ArrayList<PaletteEntry>();
        IToolBehaviorProvider toolBehaviorProvider = diagramTypeProvider.getCurrentToolBehaviorProvider();

        // Selection tool
        if (toolBehaviorProvider.isShowSelectionTool()) {
            ToolEntry tool = new GFPanningSelectionToolEntry();
            entries.add(tool);
            setDefaultEntry(tool);
        }

        // Marquee tool
        if (toolBehaviorProvider.isShowMarqueeTool()) {
            ToolEntry tool = new GFMarqueeToolEntry();
            entries.add(tool);
        }

        controlGroup.addAll(entries);
        return controlGroup;
    }

    private PaletteEntry createTool(ICreationToolEntry creationToolEntry) {
        String label = creationToolEntry.getLabel();
        String description = creationToolEntry.getDescription();
        if (creationToolEntry instanceof IObjectCreationToolEntry) {
            IObjectCreationToolEntry objectCreationToolEntry = (IObjectCreationToolEntry) creationToolEntry;
            DefaultCreationFactory cf = new DefaultCreationFactory(objectCreationToolEntry.getCreateFeature(),
                    ICreateFeature.class);
            Object template = (DND_FROM_PALETTE == true) ? cf : null;
            CombinedTemplateCreationEntry pe = new CombinedTemplateCreationEntry(label, description, template, cf,
                    getImageDescriptor(creationToolEntry, true), getImageDescriptor(creationToolEntry, false));
            pe.setToolClass(GFCreationTool.class);
            return pe;
        } else if (creationToolEntry instanceof IConnectionCreationToolEntry) {
            IConnectionCreationToolEntry connectionCreationToolEntry = (IConnectionCreationToolEntry) creationToolEntry;
            MultiCreationFactory multiCreationFactory = new MultiCreationFactory(
                    connectionCreationToolEntry.getCreateConnectionFeatures());
            ConnectionCreationToolEntry pe = new ConnectionCreationToolEntry(label, description, multiCreationFactory,
                    getImageDescriptor(creationToolEntry, true), getImageDescriptor(creationToolEntry, false));
            pe.setToolClass(CustomConnectionCreationTool.class);

            if (!diagramTypeProvider.getCurrentToolBehaviorProvider().isStayActiveAfterExecution(
                    connectionCreationToolEntry)) {
                pe.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED, true);
            }
            return pe;
        }
        return null;
    }

    private class DefaultCreationFactory implements CreationFactory {

        private Object obj;

        private Object objType;

        /**
         * 
         */
        public DefaultCreationFactory(Object obj, Object objType) {
            super();
            this.obj = obj;
            this.objType = objType;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.gef.requests.CreationFactory#getNewObject()
         */
        @Override
        public Object getNewObject() {
            return obj;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.gef.requests.CreationFactory#getObjectType()
         */
        @Override
        public Object getObjectType() {
            return objType;
        }

    }

    private ImageDescriptor getImageDescriptor(ICreationToolEntry creationToolEntry, boolean smallImage) {
        ImageDescriptor imageDescriptor = null;
        if (creationToolEntry instanceof IEclipseImageDescriptor) {
            imageDescriptor = ((IEclipseImageDescriptor) creationToolEntry).getImageDescriptor();
        } else {
            String iconId = (smallImage) ? creationToolEntry.getIconId() : creationToolEntry.getLargeIconId();
            imageDescriptor = GraphitiUi.getImageService().getImageDescriptorForId(diagramTypeProvider.getProviderId(),
                    iconId);
        }
        return imageDescriptor;
    }

    private ImageDescriptor getImageDescriptor(IPaletteCompartmentEntry compartmentEntry) {
        ImageDescriptor imageDescriptor = null;
        if (compartmentEntry instanceof IEclipseImageDescriptor) {
            imageDescriptor = ((IEclipseImageDescriptor) compartmentEntry).getImageDescriptor();
        } else {
            imageDescriptor = GraphitiUi.getImageService().getImageDescriptorForId(diagramTypeProvider.getProviderId(),
                    compartmentEntry.getIconId());
        }
        return imageDescriptor;
    }

}
