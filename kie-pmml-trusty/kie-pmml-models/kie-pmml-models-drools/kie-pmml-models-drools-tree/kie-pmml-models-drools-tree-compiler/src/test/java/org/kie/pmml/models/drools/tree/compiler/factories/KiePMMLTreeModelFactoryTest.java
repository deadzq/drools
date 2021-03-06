/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.pmml.models.drools.tree.compiler.factories;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.PMML;
import org.dmg.pmml.tree.TreeModel;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.pmml.compiler.testutils.TestUtils;
import org.kie.pmml.models.drools.ast.KiePMMLDroolsAST;
import org.kie.pmml.models.drools.tree.model.KiePMMLTreeModel;
import org.kie.pmml.models.drools.tuples.KiePMMLOriginalTypeGeneratedType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.pmml.compiler.commons.utils.JavaParserUtils.getFromFileName;
import static org.kie.pmml.models.drools.tree.compiler.factories.KiePMMLTreeModelFactory.KIE_PMML_TREE_MODEL_TEMPLATE;
import static org.kie.pmml.models.drools.tree.compiler.factories.KiePMMLTreeModelFactory.KIE_PMML_TREE_MODEL_TEMPLATE_JAVA;
import static org.kie.pmml.models.drools.utils.KiePMMLASTTestUtils.getFieldTypeMap;

public class KiePMMLTreeModelFactoryTest {

    private static final String SOURCE_1 = "TreeSample.pmml";
    private static final String TARGET_FIELD = "whatIdo";
    private static final String PACKAGE_NAME = "packagename";
    private static PMML pmml;
    private static TreeModel treeModel;
    private static ClassOrInterfaceDeclaration classOrInterfaceDeclaration;

    @BeforeClass
    public static void setUp() throws Exception {
        pmml = TestUtils.loadFromFile(SOURCE_1);
        assertNotNull(pmml);
        assertEquals(1, pmml.getModels().size());
        assertTrue(pmml.getModels().get(0) instanceof TreeModel);
        treeModel = (TreeModel) pmml.getModels().get(0);
        CompilationUnit templateCU = getFromFileName(KIE_PMML_TREE_MODEL_TEMPLATE_JAVA);
        classOrInterfaceDeclaration = templateCU
                .getClassByName(KIE_PMML_TREE_MODEL_TEMPLATE).get();
    }

    @Test
    public void getKiePMMLTreeModel() throws InstantiationException, IllegalAccessException {
        final Map<String, KiePMMLOriginalTypeGeneratedType> fieldTypeMap = getFieldTypeMap(pmml.getDataDictionary(),
                                                                                           pmml.getTransformationDictionary(),
                                                                                           treeModel.getLocalTransformations());
        KiePMMLTreeModel retrieved = KiePMMLTreeModelFactory.getKiePMMLTreeModel(pmml.getDataDictionary(),pmml.getTransformationDictionary(), treeModel, fieldTypeMap);
        assertNotNull(retrieved);
        assertEquals(treeModel.getModelName(), retrieved.getName());
        assertEquals(TARGET_FIELD, retrieved.getTargetField());
    }

    @Test
    public void getKiePMMLScorecardModelSourcesMap()  {
        final Map<String, KiePMMLOriginalTypeGeneratedType> fieldTypeMap = getFieldTypeMap(pmml.getDataDictionary(),
                                                                                           pmml.getTransformationDictionary(),
                                                                                           treeModel.getLocalTransformations());
        Map<String, String> retrieved = KiePMMLTreeModelFactory.getKiePMMLTreeModelSourcesMap(pmml.getDataDictionary(),
                                                                                                        pmml.getTransformationDictionary(),
                                                                                              treeModel,
                                                                                                        fieldTypeMap,
                                                                                                        PACKAGE_NAME);
        assertNotNull(retrieved);
        assertEquals(1, retrieved.size());
    }

    @Test
    public void getKiePMMLDroolsAST() {
        final DataDictionary dataDictionary = pmml.getDataDictionary();
        final Map<String, KiePMMLOriginalTypeGeneratedType> fieldTypeMap = getFieldTypeMap(pmml.getDataDictionary(),
                                                                                           pmml.getTransformationDictionary(),
                                                                                           treeModel.getLocalTransformations());
        KiePMMLDroolsAST retrieved = KiePMMLTreeModelFactory.getKiePMMLDroolsAST(dataDictionary, treeModel, fieldTypeMap, Collections.emptyList());
        assertNotNull(retrieved);
        List<DataField> dataFields = dataDictionary.getDataFields();
        assertEquals(dataFields.size(), fieldTypeMap.size());
        dataFields.forEach(dataField -> assertTrue(fieldTypeMap.containsKey(dataField.getName().getValue())));
    }

    @Test
    public void setSuperInvocation() {
        ConstructorDeclaration constructorDeclaration = classOrInterfaceDeclaration.getDefaultConstructor().get();
        SimpleName simpleName = new SimpleName("SIMPLENAME");
        KiePMMLTreeModelFactory.setSuperInvocation(treeModel,
                                                        constructorDeclaration,
                                                        simpleName);
        String expected = String.format("public %s() {\n" +
                                                "    super(\"%s\", Collections.emptyList(), \"%s\");\n" +
                                                "    targetField = targetField;\n" +
                                                "    pmmlMODEL = null;\n" +
                                                "}",
                                        simpleName.asString(),
                                        treeModel.getModelName(),
                                        treeModel.getAlgorithmName());
        assertEquals(expected, constructorDeclaration.toString());
    }

}