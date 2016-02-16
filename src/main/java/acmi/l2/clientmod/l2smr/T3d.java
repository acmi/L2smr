/*
 * Copyright (c) 2016 acmi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package acmi.l2.clientmod.l2smr;

import acmi.l2.clientmod.io.UnrealPackageFile;
import acmi.l2.clientmod.io.UnrealPackageReadOnly;
import acmi.l2.clientmod.unreal.classloader.L2Property;
import acmi.l2.clientmod.unreal.core.*;
import acmi.l2.clientmod.unreal.core.Enum;
import acmi.l2.clientmod.unreal.core.Object;
import acmi.l2.clientmod.unreal.objectfactory.ObjectFactory;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static acmi.l2.clientmod.util.Util.newLine;

public class T3d {
    private ObjectFactory objectFactory;

    public T3d(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public CharSequence toT3d(UnrealPackageFile.ExportEntry entry, int indent) {
        return toT3d(instantiate(entry), indent);
    }

    public CharSequence toT3d(Object object, int indent) {
        StringBuilder sb = new StringBuilder();

        sb.append("Begin Actor");
        sb.append(" Class=").append(object.getEntry().getObjectClass().getObjectName().getName());
        sb.append(" Name=").append(object.getEntry().getObjectName().getName());
        sb.append(newLine(indent + 1)).append(decompileProperties(object, indent + 1));
        sb.append(newLine(indent)).append("End Actor");

        return sb;
    }

    public CharSequence decompileProperties(Object object, int indent) {
        Stream.Builder<CharSequence> properties = Stream.builder();

        UnrealPackageReadOnly up = object.getEntry().getUnrealPackage();

        object.getProperties().forEach(property -> {
            Property template = property.getTemplate();
            for (int i = 0; i < template.arrayDimension; i++) {
                StringBuilder sb = new StringBuilder();

                java.lang.Object obj = property.getAt(i);

                if (template instanceof ByteProperty) {
                    sb.append(property.getName());
                    if (template.arrayDimension > 1) {
                        sb.append("[").append(i).append("]");
                    }
                    sb.append("=");
                    if (((ByteProperty) template).enumType != 0) {
                        UnrealPackageReadOnly.Entry enumLocalEntry = ((ByteProperty) template).getEnumType();
                        UnrealPackageReadOnly.ExportEntry enumEntry = objectFactory.getClassLoader().getExportEntry(enumLocalEntry.getObjectFullName(), e -> true);
                        Enum en = (Enum) objectFactory.apply(enumEntry);
                        sb.append(en.getValues().get((Integer) obj));
                    } else {
                        sb.append(obj);
                    }
                } else if (template instanceof IntProperty ||
                        template instanceof BoolProperty) {
                    sb.append(property.getName());
                    if (template.arrayDimension > 1) {
                        sb.append("[").append(i).append("]");
                    }
                    sb.append("=");
                    sb.append(obj);
                } else if (template instanceof FloatProperty) {
                    sb.append(property.getName());
                    if (template.arrayDimension > 1) {
                        sb.append("[").append(i).append("]");
                    }
                    sb.append("=");
                    sb.append(String.format(Locale.US, "%f", (Float) obj));
                } else if (template instanceof ObjectProperty) {
                    UnrealPackageReadOnly.Entry entry = up.objectReference((Integer) obj);
                    if (needExport(entry, template)) {
                        properties.add(toT3d(instantiate((UnrealPackageReadOnly.ExportEntry) entry), indent));
                    }
                    sb.append(property.getName());
                    if (template.arrayDimension > 1) {
                        sb.append("[").append(i).append("]");
                    }
                    sb.append("=");
                    if (entry == null) {
                        sb.append("None");
                    } else if (entry instanceof UnrealPackageReadOnly.ImportEntry) {
                        sb.append(((UnrealPackageReadOnly.ImportEntry) entry).getClassName().getName())
                                .append("'")
                                .append(entry.getObjectFullName())
                                .append("'");
                    } else if (entry instanceof UnrealPackageReadOnly.ExportEntry) {
                        String clazz = "Class";
                        if (((UnrealPackageReadOnly.ExportEntry) entry).getObjectClass() != null)
                            clazz = ((UnrealPackageReadOnly.ExportEntry) entry).getObjectClass().getObjectName().getName();
                        sb.append(clazz)
                                .append("'")
                                .append(entry.getObjectInnerFullName())
                                .append("'");
                    } else {
                        throw new IllegalStateException("wtf");
                    }
                } else if (template instanceof NameProperty) {
                    sb.append(property.getName());
                    if (template.arrayDimension > 1) {
                        sb.append("[").append(i).append("]");
                    }
                    sb.append("=");
                    sb.append("'").append(Objects.toString(obj)).append("'");
                } else if (template instanceof ArrayProperty) {
                    ArrayProperty arrayProperty = (ArrayProperty) property.getTemplate();
                    Property innerProperty = (Property) objectFactory.apply(objectFactory.getClassLoader().getExportEntry(arrayProperty.getInner().getObjectFullName(), e -> true));
                    L2Property fakeProperty = new L2Property(innerProperty, up);
                    List<java.lang.Object> list = (List<java.lang.Object>) obj;

                    for (int j = 0; j < list.size(); j++) {
                        java.lang.Object innerObj = list.get(j);

                        if (innerProperty instanceof ObjectProperty) {
                            UnrealPackageReadOnly.Entry entry = up.objectReference((Integer) innerObj);
                            if (needExport(entry, innerProperty)) {
                                properties.add(toT3d(instantiate((UnrealPackageReadOnly.ExportEntry) entry), indent));
                            }
                        }

                        fakeProperty.putAt(0, innerObj);
                        if (j > 0)
                            sb.append(newLine(indent));
                        sb.append(property.getName()).append("(").append(j).append(")")
                                .append("=")
                                .append(inlineProperty(fakeProperty, up, true));
                    }
                } else if (template instanceof StructProperty) {
                    sb.append(property.getName());
                    if (template.arrayDimension > 1) {
                        sb.append("[").append(i).append("]");
                    }
                    sb.append("=");
                    if (obj == null) {
                        sb.append("None");
                    } else {
                        sb.append(inlineStruct((List<L2Property>) obj, up));
                    }
                } else if (template instanceof StrProperty) {
                    sb.append(property.getName());
                    if (template.arrayDimension > 1) {
                        sb.append("[").append(i).append("]");
                    }
                    sb.append("=");
                    sb.append("\"").append(Objects.toString(obj)).append("\"");
                }
                properties.add(sb);
            }
        });

        return properties.build().collect(Collectors.joining(newLine(indent)));
    }

    public CharSequence inlineProperty(L2Property property, UnrealPackageReadOnly up, boolean valueOnly) {
        StringBuilder sb = new StringBuilder();

        Property template = property.getTemplate();

        for (int i = 0; i < template.arrayDimension; i++) {
            if (!valueOnly) {
                sb.append(property.getName());

                if (template.arrayDimension > 1) {
                    sb.append("[").append(i).append("]");
                }

                sb.append("=");
            }

            java.lang.Object object = property.getAt(i);

            if (template instanceof ByteProperty) {
                if (((ByteProperty) template).enumType != 0) {
                    UnrealPackageReadOnly.Entry enumLocalEntry = ((ByteProperty) template).getEnumType();
                    UnrealPackageReadOnly.ExportEntry enumEntry = objectFactory.getClassLoader().getExportEntry(enumLocalEntry.getObjectFullName(), e -> true);
                    Enum en = (Enum) objectFactory.apply(enumEntry);
                    sb.append(en.getValues().get((Integer) object));
                } else {
                    sb.append(object);
                }
            } else if (template instanceof IntProperty ||
                    template instanceof BoolProperty) {
                sb.append(object);
            } else if (template instanceof FloatProperty) {
                sb.append(String.format(Locale.US, "%f", (Float) object));
            } else if (template instanceof ObjectProperty) {
                UnrealPackageReadOnly.Entry entry = up.objectReference((Integer) object);
                if (entry == null) {
                    sb.append("None");
                } else if (entry instanceof UnrealPackageReadOnly.ImportEntry) {
                    sb.append(((UnrealPackageReadOnly.ImportEntry) entry).getClassName().getName())
                            .append("'")
                            .append(entry.getObjectFullName())
                            .append("'");
                } else if (entry instanceof UnrealPackageReadOnly.ExportEntry) {
                    if (template.getPropertyFlags().contains(Property.CPF.ExportObject)) {
                        sb.append("\"").append(entry.getObjectName().getName()).append("\"");
                    } else {
                        String clazz = "Class";
                        if (((UnrealPackageReadOnly.ExportEntry) entry).getObjectClass() != null)
                            clazz = ((UnrealPackageReadOnly.ExportEntry) entry).getObjectClass().getObjectName().getName();
                        sb.append(clazz)
                                .append("'")
                                .append(entry.getObjectName().getName())
                                .append("'");
                    }
                } else {
                    throw new IllegalStateException("wtf");
                }
            } else if (template instanceof NameProperty) {
                sb.append("'").append(Objects.toString(object)).append("'");
            } else if (template instanceof ArrayProperty) {
                ArrayProperty arrayProperty = (ArrayProperty) property.getTemplate();
                Property innerProperty = (Property) objectFactory.apply(objectFactory.getClassLoader().getExportEntry(arrayProperty.getInner().getObjectFullName(), e -> true));
                L2Property fakeProperty = new L2Property(innerProperty, up);
                List<java.lang.Object> list = (List<java.lang.Object>) object;

                sb.append(list.stream()
                        .map(o -> {
                            fakeProperty.putAt(0, o);
                            return inlineProperty(fakeProperty, up, true);
                        }).collect(Collectors.joining(",", "(", ")")));
            } else if (template instanceof StructProperty) {
                if (object == null) {
                    sb.append("None");
                } else {
                    sb.append(inlineStruct((List<L2Property>) object, up));
                }
            } else if (template instanceof StrProperty) {
                sb.append("\"").append(Objects.toString(object)).append("\"");
            }

            if (i != template.arrayDimension - 1)
                sb.append(",");
        }

        return sb;
    }

    public CharSequence inlineStruct(List<L2Property> struct, UnrealPackageReadOnly up) {
        return struct.stream().map(p -> inlineProperty(p, up, false)).collect(Collectors.joining(",", "(", ")"));
    }

    public boolean needExport(UnrealPackageReadOnly.Entry entry, Property template) {
        return entry != null &&
                entry instanceof UnrealPackageReadOnly.ExportEntry &&
                (template.getPropertyFlags().contains(Property.CPF.ExportObject));

    }

    private Object instantiate(UnrealPackageReadOnly.ExportEntry entry) {
        String objClass = entry.getObjectClass() == null ? "Core.Class" : entry.getObjectClass().getObjectFullName();
        if (objClass.equals("Core.Class") ||
                objClass.equals("Core.State") ||
                objClass.equals("Core.Function") ||
                objClass.equals("Core.Struct")) {
            return objectFactory.getClassLoader().getStruct(entry.getObjectFullName());
        } else {
            return objectFactory.apply(entry);
        }
    }
}
