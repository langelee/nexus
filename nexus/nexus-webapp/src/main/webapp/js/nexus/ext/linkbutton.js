/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global define*/

//
// Based on: http://www.sencha.com/forum/showthread.php?54602-Link-Button-or-Hyper-link&p=260201&viewfull=1#post260201
//

/**
 * A button which is rendered as a link.
 *
 * @class Nexus.ext.LinkButton
 * @extends Ext.Button
 * @namespace Nexus
 */
define('nexus/ext/linkbutton', ['extjs', 'nexus'], function(Ext, Nexus) {
  Ext.namespace('Nexus.ext');

  var LinkButton = Ext.extend(Ext.Button, {
    template : new Ext.Template(
          '<table cellspacing="0" class="x-btn {3}"><tbody class="{4}">',
          '<tr>',
          '<td class="x-linkbtn-tl"><i> </i></td>',
          '<td class="x-linkbtn-tc"></td>',
          '<td class="x-linkbtn-tr"><i> </i></td>',
          '</tr>',
          '<tr>',
          '<td class="x-linkbtn-ml"><i> </i></td>',
          '<td class="x-linkbtn-mc">',
          '<em class="{5}" unselectable="on">',
          '<a href="{6}" style="display:block" target="{7}" class="x-linkbtn-text {2}" style="text-decoration: none; color: black; padding-left: 3px; padding-right: 3px;">{0}</a>',
          '</em>',
          '</td>',
          '<td class="x-linkbtn-mr"><i> </i></td>',
          '</tr>',
          '<tr>',
          '<td class="x-linkbtn-bl"><i> </i></td>',
          '<td class="x-linkbtn-bc"></td>',
          '<td class="x-linkbtn-br"><i> </i></td>',
          '</tr>',
          '</tbody></table>' ).compile(),

    buttonSelector : 'a:first',

    /**
     * @cfg String href
     * The URL to create a link for.
     */
    /**
     * @cfg String target
     * The target for the &lt;a> element.
     */
    /**
     * @cfg Object
     * A set of parameters which are always passed to the URL specified in the href
     */
    baseParams : {},

    params : {},

    getTemplateArgs : function() {
      return Ext.Button.prototype.getTemplateArgs.apply(this).concat([this.getHref(), this.target]);
    },

    onClick : function(e) {
      if (e.button !== 0) {
        return;
      }
      if (this.disabled) {
        this.stopEvent(e);
      } else {
        if (this.fireEvent("click", this, e) !== false) {
          if (this.handler) {
            this.handler.call(this.scope || this, this, e);
          }
        }
      }
    },

    // private
    getHref : function() {
      var
            result = this.href,
            p = Ext.urlEncode(Ext.apply(Ext.apply({}, this.baseParams), this.params));

      if (p.length) {
        result += ((this.href.indexOf('?') === -1) ? '?' : '&') + p;
      }
      return result;
    },

    /**
     * Sets the href of the link dynamically according to the params passed, and any {@link #baseParams} configured.
     * @param {Object} Parameters to use in the href URL.
     */
    setParams : function(p) {
      this.params = p;
      this.el.child(this.buttonSelector, true).href = this.getHref();
    }
  });

  Nexus.ext.LinkButton = LinkButton;
  Ext.ComponentMgr.registerType('link-button', Nexus.ext.LinkButton);
});

