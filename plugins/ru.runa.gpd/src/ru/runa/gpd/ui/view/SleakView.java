package ru.runa.gpd.ui.view;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.util.UiUtil;

public class SleakView extends ViewPart {
    public static final String ID = "ru.runa.gpd.sleak";
    private List list;
    private Canvas canvas;
    private Text stackTraceText;
    private Text infoText;
    private Object[] oldObjects = new Object[0];
    private Object[] objects = new Object[0];
    private Error[] errors = new Error[0];

    @Override
    public void createPartControl(Composite parent) {
        UiUtil.hideToolBar(getViewSite());
        if (!Display.getCurrent().getDeviceData().tracking) {
            Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout());
            String info = Localization.getString("sleak.notInDebugMode");
            info += "\n\nExtract the .options file from the plugins/org.eclipse.ui_<version>.jar archive to the root folder.";
            info += "\n     Add the following lines to the .options file:";
            info += "\n     org.eclipse.ui/debug=true";
            info += "\n     org.eclipse.ui/trace/graphics=true";
            info += "\nLaunch application with the '-debug' option on the command line.\n\n\n";
            SWTUtils.createLabel(composite, info);
            Text text = new Text(composite, SWT.READ_ONLY);
            text.setText("http://www.eclipse.org/swt/tools.php");
            text = new Text(composite, SWT.READ_ONLY);
            text.setText("https://bugs.eclipse.org/bugs/show_bug.cgi?id=74517");
            return;
        }
        SashForm composite = new SashForm(parent, SWT.HORIZONTAL);
        composite.setLayout(new GridLayout(3, false));
        Composite leftComposite = new Composite(composite, SWT.NONE);
        leftComposite.setLayout(new GridLayout());
        leftComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        Composite centerComposite = new Composite(composite, SWT.NONE);
        centerComposite.setLayout(new GridLayout());
        centerComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        centerComposite.setSize(200, 100);
        SashForm rightComposite = new SashForm(composite, SWT.VERTICAL);
        rightComposite.setLayout(new GridLayout());
        rightComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setWeights(new int[] { 2, 2, 7 });
        Button snapButton = new Button(leftComposite, SWT.PUSH);
        snapButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        snapButton.setText(Localization.getString("sleak.button.snap"));
        snapButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                refreshAll();
            }
        });
        Button diffButton = new Button(leftComposite, SWT.PUSH);
        diffButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        diffButton.setText(Localization.getString("sleak.button.diff"));
        diffButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                refreshDifference();
            }
        });
        infoText = new Text(leftComposite, SWT.MULTI | SWT.READ_ONLY);
        infoText.setLayoutData(new GridData(GridData.FILL_BOTH));
        infoText.setText("0 object(s)");
        Button dumpButton = new Button(leftComposite, SWT.PUSH);
        dumpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dumpButton.setText(Localization.getString("sleak.button.dump"));
        dumpButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                dumpToFile();
            }
        });
        list = new List(centerComposite, SWT.BORDER | SWT.V_SCROLL);
        list.setLayoutData(new GridData(GridData.FILL_BOTH));
        list.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                refreshObject();
            }
        });
        canvas = new Canvas(rightComposite, SWT.BORDER);
        canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
        canvas.addListener(SWT.Paint, new Listener() {
            @Override
            public void handleEvent(Event event) {
                paintCanvas(event);
            }
        });
        stackTraceText = new Text(rightComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        stackTraceText.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    @Override
    public void setFocus() {
    }

    private String getInfo() {
        int objectsLength = 0, colors = 0, cursors = 0, fonts = 0, gcs = 0, images = 0;
        int paths = 0, patterns = 0, regions = 0, textLayouts = 0, transforms = 0;
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            if (object instanceof Color) {
                if (((Color) object).isDisposed()) {
                    continue;
                }
                colors++;
            }
            if (object instanceof Cursor) {
                if (((Cursor) object).isDisposed()) {
                    continue;
                }
                cursors++;
            }
            if (object instanceof Font) {
                if (((Font) object).isDisposed()) {
                    continue;
                }
                fonts++;
            }
            if (object instanceof GC) {
                if (((GC) object).isDisposed()) {
                    continue;
                }
                gcs++;
            }
            if (object instanceof Image) {
                if (((Image) object).isDisposed()) {
                    continue;
                }
                images++;
            }
            if (object instanceof Path) {
                if (((Path) object).isDisposed()) {
                    continue;
                }
                paths++;
            }
            if (object instanceof Pattern) {
                if (((Pattern) object).isDisposed()) {
                    continue;
                }
                patterns++;
            }
            if (object instanceof Region) {
                if (((Region) object).isDisposed()) {
                    continue;
                }
                regions++;
            }
            if (object instanceof TextLayout) {
                if (((TextLayout) object).isDisposed()) {
                    continue;
                }
                textLayouts++;
            }
            if (object instanceof Transform) {
                if (((Transform) object).isDisposed()) {
                    continue;
                }
                transforms++;
            }
        }
        String string = "";
        if (colors != 0) {
            string += colors + " Color(s)\n";
            objectsLength += colors;
        }
        if (cursors != 0) {
            string += cursors + " Cursor(s)\n";
            objectsLength += cursors;
        }
        if (fonts != 0) {
            string += fonts + " Font(s)\n";
            objectsLength += fonts;
        }
        if (gcs != 0) {
            string += gcs + " GC(s)\n";
            objectsLength += gcs;
        }
        if (images != 0) {
            string += images + " Image(s)\n";
            objectsLength += images;
        }
        if (paths != 0) {
            string += paths + " Paths(s)\n";
            objectsLength += paths;
        }
        if (patterns != 0) {
            string += patterns + " Pattern(s)\n";
            objectsLength += patterns;
        }
        if (regions != 0) {
            string += regions + " Region(s)\n";
            objectsLength += regions;
        }
        if (textLayouts != 0) {
            string += textLayouts + " TextLayout(s)\n";
            objectsLength += textLayouts;
        }
        if (transforms != 0) {
            string += transforms + " Transform(s)\n";
            objectsLength += transforms;
        }
        return objectsLength + " Object(s):\n\n" + string;
    }

    private void refreshDifference() {
        DeviceData info = Display.getDefault().getDeviceData();
        Object[] newObjects = info.objects;
        Error[] newErrors = info.errors;
        Object[] diffObjects = new Object[newObjects.length];
        Error[] diffErrors = new Error[newErrors.length];
        int count = 0;
        for (int i = 0; i < newObjects.length; i++) {
            int index = 0;
            while (index < oldObjects.length) {
                if (newObjects[i] == oldObjects[index]) {
                    break;
                }
                index++;
            }
            if (index == oldObjects.length) {
                diffObjects[count] = newObjects[i];
                diffErrors[count] = newErrors[i];
                count++;
            }
        }
        objects = new Object[count];
        errors = new Error[count];
        System.arraycopy(diffObjects, 0, objects, 0, count);
        System.arraycopy(diffErrors, 0, errors, 0, count);
        list.removeAll();
        stackTraceText.setText("");
        canvas.redraw();
        for (int i = 0; i < objects.length; i++) {
            list.add(objects[i].toString());
        }
        infoText.setText(getInfo());
    }

    private void paintCanvas(Event event) {
        canvas.setCursor(null);
        int index = list.getSelectionIndex();
        if (index == -1) {
            return;
        }
        GC gc = event.gc;
        Object object = objects[index];
        if (object instanceof Color) {
            if (((Color) object).isDisposed()) {
                return;
            }
            gc.setBackground((Color) object);
            gc.fillRectangle(canvas.getClientArea());
            return;
        }
        if (object instanceof Cursor) {
            if (((Cursor) object).isDisposed()) {
                return;
            }
            canvas.setCursor((Cursor) object);
            return;
        }
        if (object instanceof Font) {
            if (((Font) object).isDisposed()) {
                return;
            }
            gc.setFont((Font) object);
            FontData[] array = gc.getFont().getFontData();
            String string = "";
            String lf = stackTraceText.getLineDelimiter();
            for (int i = 0; i < array.length; i++) {
                FontData data = array[i];
                String style = "NORMAL";
                int bits = data.getStyle();
                if (bits != 0) {
                    if ((bits & SWT.BOLD) != 0) {
                        style = "BOLD ";
                    }
                    if ((bits & SWT.ITALIC) != 0) {
                        style += "ITALIC";
                    }
                }
                string += data.getName() + " " + data.getHeight() + " " + style + lf;
            }
            gc.drawString(string, 0, 0);
            return;
        }
        // NOTHING TO DRAW FOR GC
        // if (object instanceof GC) {
        // return;
        // }
        if (object instanceof Image) {
            if (((Image) object).isDisposed()) {
                return;
            }
            gc.drawImage((Image) object, 0, 0);
            return;
        }
        if (object instanceof Path) {
            if (((Path) object).isDisposed()) {
                return;
            }
            gc.drawPath((Path) object);
            return;
        }
        if (object instanceof Pattern) {
            if (((Pattern) object).isDisposed()) {
                return;
            }
            gc.setBackgroundPattern((Pattern) object);
            gc.fillRectangle(canvas.getClientArea());
            gc.setBackgroundPattern(null);
            return;
        }
        if (object instanceof Region) {
            if (((Region) object).isDisposed()) {
                return;
            }
            String string = ((Region) object).getBounds().toString();
            gc.drawString(string, 0, 0);
            return;
        }
        if (object instanceof TextLayout) {
            if (((TextLayout) object).isDisposed()) {
                return;
            }
            ((TextLayout) object).draw(gc, 0, 0);
            return;
        }
        if (object instanceof Transform) {
            if (((Transform) object).isDisposed()) {
                return;
            }
            String string = ((Transform) object).toString();
            gc.drawString(string, 0, 0);
            return;
        }
    }

    private void refreshObject() {
        int index = list.getSelectionIndex();
        if (index == -1) {
            return;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream s = new PrintStream(stream);
        errors[index].printStackTrace(s);
        stackTraceText.setText(stream.toString());
        canvas.redraw();
    }

    private void refreshAll() {
        oldObjects = new Object[0];
        refreshDifference();
        oldObjects = objects;
    }

    private void dumpToFile() {
        try {
            StringBuffer from = new StringBuffer();
            from.append(getInfo()).append("\n\n");
            Map<String, java.util.List<String>> stackTraceToObjects = Maps.newHashMap();
            int max = 0;
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                String objectInfo = object.toString();
                if (object instanceof Color) {
                    if (((Color) object).isDisposed()) {
                        continue;
                    }
                }
                if (object instanceof Cursor) {
                    if (((Cursor) object).isDisposed()) {
                        continue;
                    }
                }
                if (object instanceof Font) {
                    if (((Font) object).isDisposed()) {
                        continue;
                    }
                }
                if (object instanceof GC) {
                    if (((GC) object).isDisposed()) {
                        continue;
                    }
                }
                if (object instanceof Image) {
                    if (((Image) object).isDisposed()) {
                        continue;
                    }
                }
                if (object instanceof Path) {
                    if (((Path) object).isDisposed()) {
                        continue;
                    }
                }
                if (object instanceof Pattern) {
                    if (((Pattern) object).isDisposed()) {
                        continue;
                    }
                }
                if (object instanceof Region) {
                    if (((Region) object).isDisposed()) {
                        continue;
                    }
                }
                if (object instanceof TextLayout) {
                    if (((TextLayout) object).isDisposed()) {
                        continue;
                    }
                    objectInfo += " " + ((TextLayout) object).getText();
                }
                if (object instanceof Transform) {
                    if (((Transform) object).isDisposed()) {
                        continue;
                    }
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                PrintStream s = new PrintStream(stream);
                errors[i].printStackTrace(s);
                String stackTrace = stream.toString();
                java.util.List<String> list = stackTraceToObjects.get(stackTrace);
                if (list == null) {
                    list = Lists.newArrayList();
                    stackTraceToObjects.put(stackTrace, list);
                }
                list.add(objectInfo);
                if (list.size() > max) {
                    max = list.size();
                }
            }
            for (int i = max; i >= 0; i--) {
                for (Map.Entry<String, java.util.List<String>> entry : stackTraceToObjects.entrySet()) {
                    if (entry.getValue().size() == i) {
                        from.append(i).append("\n").append(entry.getValue()).append("\n");
                        from.append(entry.getKey()).append("\n\n");
                    }
                }
            }
            Files.write(from, new File("swt.debug.dump"), Charsets.UTF_8);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }
}
