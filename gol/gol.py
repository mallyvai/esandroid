#!/usr/bin/env python
# coding=utf-8

'''
 pyGameOfLife
 Author : Guillaume "iXce" Seguin
 Email  : guillaume@segu.in (or ixce@beryl-project.org)

 # Simple PyGTK Game Of Life #

 Copyright (C) 2007 Guillaume Seguin

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor,
 Boston, MA  02110-1301, USA.
'''

import pygtk
pygtk.require ('2.0')
import gtk
import gobject
import gtk.glade

import random
import cairo
import pangocairo
from math import pi

def autoconnect (ob):
    '''Autoconnect every member from ob to its glade interface'''
    handlers = {}
    for i in dir (ob):
        handlers[i] = getattr (ob, i)
    ob.glade.signal_autoconnect (handlers);

def draw_rounded_rectangle (cr, x, y, width, height):
    '''Draw a rounded rectangle at x, y on Cairo context cr'''
    radius = 30
    x0 = x + 10
    y0 = y + 10
    x1 = x + width - 10
    y1 = y + height - 10
    cr.new_path ()
    cr.arc (x0 + radius, y1 - radius, radius, pi / 2, pi)
    cr.line_to (x0, y0 + radius)
    cr.arc (x0 + radius, y0 + radius, radius, pi, 3 * pi / 2)
    cr.line_to (x1 - radius, y0)
    cr.arc (x1 - radius, y0 + radius, radius, 3 * pi / 2, 2 * pi)
    cr.line_to (x1, y1 - radius)
    cr.arc (x1 - radius, y1 - radius, radius, 0, pi / 2)
    cr.close_path ()

def prepare_text (cr, text):
    '''Prepare text for drawing with Pango/Cairo'''
    pcr = pangocairo.CairoContext (cr)
    layout = pcr.create_layout ()
    layout.set_markup (text)
    return pcr, layout

class gtkGameOfLife (gtk.DrawingArea):

    _parent      = None
    _surface     = None
    _map         = None
    _finished    = False
    _size        = 0
    _cell_side   = 10
    _timeout     = 100
    _source      = None

    def __init__ (self, side = 300, parent = None):
        '''Prepare widget'''
        super (gtkGameOfLife, self).__init__ ()
        self._parent = parent
        self._size = side / self._cell_side
        side = self._size * self._cell_side
        self.add_events (gtk.gdk.BUTTON_PRESS_MASK)
        self.connect ("expose-event", self.expose)
        self.connect ("map", self.on_map)
        self.connect ("button-press-event", self.button_press)
        self.set_size_request (side, side)
        self.new_game ()

    def is_finished (self):
        '''Check if the game is finished, ie the board is empty'''
        for row in self._map:
            if len (filter (lambda l: l != 0, row)):
                return False
        return True

    def new_game (self):
        '''Reset game'''
        self._map = []
        for i in range (self._size):
            self._map.append ([0] * self._size)
        todo = 200
        while todo > 0:
            i = random.randint (0, self._size - 1)
            j = random.randint (0, self._size - 1)
            if not self._map[i][j]:
                self._map[i][j] = 1
                todo -= 1
        self._finished = False
        if self._surface:
            self.redraw (queue = True)

    def on_map (self, *args):
        '''Initial map callback that starts the timeout'''
        if self._source:
            return
        self.source = gobject.timeout_add (self._timeout, self.update_map)

    def surrounding (self, i, j):
        '''Counts the surrounding alive points around point (i, j)'''
        surrounding = 0
        for x in range (-1, 2):
            i2 = (i + x + self._size) % self._size
            for y in range (-1, 2):
                j2 = (j + y + self._size) % self._size
                if self._map[i2][j2]:
                    surrounding += 1
        return surrounding

    def update_map (self):
        '''Update the Game Map''' 
        if self._finished:
            return True
        new_map = []
        for i in range (self._size):
            new_map.append ([0] * self._size)
        for i in range (self._size):
            for j in range (self._size):
                surrounding = self.surrounding (i, j)
                if (self._map[i][j] and surrounding in (2, 3)) or surrounding == 3:
                    new_map[i][j] = 1
        self._map = new_map
        if self.is_finished ():
            self._finished = True
            if self._source:
                gobject.source_remove (self._source)
                self._source = None
        if self._surface:
            self.redraw (queue = True)
        return True

    def button_press (self, widget, event):
        '''Check if the cursor clicked an interesting area'''
        if self._finished:
            self.new_game ()
            return
        '''Add a bunch of dots around event point, just for fun'''
        alloc = self.get_allocation ()
        cell_side = self._cell_side
        i = (event.x - (event.x % cell_side)) / cell_side
        j = (event.y - (event.y % cell_side)) / cell_side
        i = int (min (i, self._size - 1))
        j = int (min (j, self._size - 1))
        jplusone = (j + 1) % self._size
        jminusone = (j - 1 + self._size) % self._size
        self._map[i][j] = self._map[i][jplusone] = 1
        self._map[i][jminusone] = 1

    def redraw (self, queue = False):
        '''Redraw internal surface'''
        alloc = self.get_allocation ()
        side = min (alloc.width, alloc.height)
        cell_side = self._cell_side
        self._surface = cairo.ImageSurface (cairo.FORMAT_ARGB32, side, side)
        cr = cairo.Context (self._surface)
        # Draw background
        cr.set_source_rgb (1, 1, 1)
        cr.paint ()
        # Draw cells
        for i in range (self._size):
            for j in range (self._size):
                if not self._map[i][j]:
                    continue
                # Pretty color!
                color = 0.75 + float (i + j) / (8 * self._size)
                cr.set_source_rgb (color, 0, 0)
                cr.rectangle (cell_side * i, cell_side * j,
                              cell_side, cell_side)
                cr.fill ()
        if self._finished:
            cr.set_source_rgba (0.8, 0.8, 0.8, 0.5)
            cr.paint ()
            cr.set_source_rgba (0.2, 0.2, 0.2, 0.6)
            draw_rounded_rectangle (cr, side * 0.1, side * 0.25,
                                    side * 0.8, side * 0.5)
            cr.fill ()
            cr.set_source_rgb (1, 1, 1)
            cr.move_to (side * 0.25, side * 0.43)
            text =  '<span font_desc="Sans 25"><b>Replay ?</b></span>'
            pcr, layout = prepare_text (cr, text)
            pcr.show_layout (layout)
        if queue:
            self.queue_draw ()

    def expose (self, widget, event):
        '''Expose event handler'''
        cr = self.window.cairo_create ()
        if not self._surface:
            self.redraw ()
        cr.set_source_surface (self._surface)
        cr.rectangle (event.area.x, event.area.y,
                      event.area.width, event.area.height)
        cr.clip ()
        cr.paint ()
        return False

class pyGameOfLife:

    glade           = None
    mainWindow	    = None
    aboutDialog	    = None
    mainFrame       = None
    statusBar       = None
    messageDialog   = None
    game            = None

    def __init__ (self):
        '''Initialize application'''
        self.glade = gtk.glade.XML (fname = "gol.glade")
        self.mainWindow = self.glade.get_widget ("mainWindow")
        self.aboutDialog = self.glade.get_widget ("aboutDialog")
        self.mainFrame = self.glade.get_widget ("mainFrame")
        self.statusBar = self.glade.get_widget ("statusBar")
        self.messageDialog = self.glade.get_widget ("messageDialog")
        self.glade.get_widget ("toolBar").set_style (gtk.TOOLBAR_ICONS)
        autoconnect (self)
        self.game = gtkGameOfLife (parent = self)
        self.mainFrame.add (self.game)

    def new_game (self, *args):
        '''Starts a new game'''
        self.push_status ("")
        self.game.new_game ()

    def push_status (self, text):
        '''Update status bar message'''
        context = self.statusBar.get_context_id ("main")
        self.statusBar.pop (context)
        self.statusBar.push (context, text)

    def show (self):
        '''Show application'''
        self.mainWindow.show_all ()

    def gtk_main_quit (self, *args):
        '''Quit gtk main loop'''
        gtk.main_quit ()

    def show_about (self, *args):
        '''Show about dialog'''
        self.aboutDialog.show ()

    def close_about (self, *args):
        '''Hide about dialog'''
        self.aboutDialog.hide ()
        return True

    def open_message_dialog (self, text):
        '''Open the message dialog and set the message'''
        text = '''<span font_desc="Sans 16"><b>%s</b></span>''' % text
        self.messageDialog.set_markup (text)
        self.messageDialog.show ()

    def close_message_dialog (self, *args):
        '''Close message dialog'''
        self.messageDialog.hide ()
        return False

if __name__ == "__main__":
    game = pyGameOfLife ()
    game.show ()
    gtk.main ()
