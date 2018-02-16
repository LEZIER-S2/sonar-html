/*
 * SonarWeb :: SonarQube Plugin
 * Copyright (c) 2010-2018 SonarSource SA and Matthijs Galesloot
 * sonarqube@googlegroups.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonar.plugins.web.checks.dependencies;

import com.google.common.base.Strings;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.web.checks.AbstractPageCheck;
import org.sonar.plugins.web.node.Attribute;
import org.sonar.plugins.web.node.DirectiveNode;
import org.sonar.plugins.web.node.Node;
import org.sonar.plugins.web.node.TagNode;

import java.util.List;

@Rule(key = "IllegalTagLibsCheck")
public class IllegalTagLibsCheck extends AbstractPageCheck {

  private static final String DEFAULT_TAG_LIBS = "http://java.sun.com/jstl/sql";

  @RuleProperty(
    key = "tagLibs",
    description = "Comma-separated list of URIs of disallowed taglibs",
    defaultValue = DEFAULT_TAG_LIBS)
  public String tagLibs = DEFAULT_TAG_LIBS;

  private String[] tagLibsArray;

  @Override
  public void startDocument(List<Node> nodes) {
    tagLibsArray = trimSplitCommaSeparatedList(tagLibs);
  }

  @Override
  public void startElement(TagNode node) {
    if ("jsp:directive.taglib".equalsIgnoreCase(node.getNodeName())) {
      checkIt(node, node.getAttribute("uri"));
    }
  }

  private void checkIt(Node node, String uri) {
    if (!Strings.isNullOrEmpty(uri)) {
      for (String tagLib : tagLibsArray) {
        if (tagLib.equalsIgnoreCase(uri)) {
          createViolation(node.getStartLinePosition(), "Remove the use of \"" + tagLib + "\".");
        }
      }
    }
  }

  @Override
  public void directive(DirectiveNode node) {
    if ("taglib".equalsIgnoreCase(node.getNodeName())) {
      for (Attribute a : node.getAttributes()) {
        checkIt(node, a.getValue());
      }
    }
  }

}
