//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

import com.intellij.openapi.actionSystem.AnActionEvent
import static liveplugin.PluginUtil.*

// The document roughly corresponds to an emacs buffer. The document
// manages a mutable Java CharSequence with a few simple methods for
// inserting, replacing and deleting text. The `runDocumentWriteAction`
// provides thread safety and undo.
//
// The caretModel.offset is equivalent to emacs point. It is an int
// that ranges from 0 to the bufer length (so watch for off-by-one
// when porting elisp!) The caretModel is automatically updated as
// changes are made to the document, but if you save the offset in a
// local var, you have to keep things consistent yourself.

registerAction("delete-char", "ctrl D") { AnActionEvent event ->
    runDocumentWriteAction(event.project) {
        currentEditorIn(event.project).with {
            def point = caretModel.offset
            if (point < document.textLength) {
                document.deleteString(point, point + 1)
            }
        }
    }
}

registerAction("transpose-chars", "ctrl T") { AnActionEvent event ->
    runDocumentWriteAction(event.project) {
        currentEditorIn(event.project).with {
            def offset = caretModel.offset
            def currentLine = caretModel.logicalPosition.line
            def lineEndOffset = document.getLineEndOffset(currentLine)
            if (offset == lineEndOffset && offset > 1) {
                offset -= 1
                caretModel.moveToOffset(offset)
            }
            if (offset > 1) {
                def buf = document.getCharsSequence()
                def swapped = [buf.charAt(offset), buf.charAt(offset-1)] as char[]
                document.replaceString(offset-1, offset+1, swapped.toString())
                if (offset < document.textLength) {
                    caretModel.moveToOffset(offset + 1)
                }
            }
        }
    }
}

class TransposeWords {
    final buf
    final textLength
    final offset
    def newpos
    TransposeWords(theBuf, theOffset) {
        buf = theBuf
        offset = theOffset
        textLength = buf.size()
        newpos = theOffset
    }
    def isPartOfWord(theOffset) {
        return (theOffset >=0 && theOffset < textLength) ? Character.isLetterOrDigit(buf.charAt(theOffset)) : false
    }
    def scanForwardNonWord(origin) {
        for (i in origin..textLength) {
            if (!isPartOfWord(i)) {
                return i
            }
        }
        textLength
    }
    def scanBackwardNonWord(origin) {
        for (i in origin..0) {
            if (!isPartOfWord(i)) {
                return i + 1
            }
        }
        0
    }
    def scanForwardWord(origin) {
        for (i in origin..textLength) {
            if (isPartOfWord(i)) {
                return i
            }
        }
        textLength
    }
    def scanBackwardWord(origin) {
        for (i in origin..0) {
            if (isPartOfWord(i)) {
                return i
            }
        }
        0
    }
    def findWord0() {
        def start, end
        if (isPartOfWord(offset)) {
            end = scanForwardNonWord(offset)
            start = scanBackwardNonWord(offset)
            [start, end]
        }
        else {
            start = scanForwardWord(offset)
            end = scanForwardNonWord(start)
            [start, end]
        }
    }
    def findWord1(word0) {
        def start, end
        if (offset <= word0[0]) {
            end = scanBackwardWord(word0[0] - 1) + 1
            start = scanBackwardNonWord(end - 1)
            [start, end]
        }
        else {
            start = scanForwardWord(word0[1])
            end = scanForwardNonWord(start)
            [start, end]
        }
    }
    def transpose(document) {
        def (start0, end0) = findWord0()
        def (start1, end1) = findWord1([start0, end0])
        def word0 = buf.subSequence(start0, end0)
        def word1 = buf.subSequence(start1, end1)
        if (start0 < start1) {
            document.replaceString(start1, end1, word0)
            document.replaceString(start0, end0, word1)
            newpos = end1
        }
        else {
            document.replaceString(start0, end0, word1)
            document.replaceString(start1, end1, word0)
            newpos = end0
        }
    }
}

registerAction("transpose-words", "meta T") { AnActionEvent event ->
    runDocumentWriteAction(event.project) {
        currentEditorIn(event.project).with {
            def offset = caretModel.offset
            def tw = new TransposeWords(document.getCharsSequence(), offset)
            tw.transpose(document)
            caretModel.moveToOffset(tw.newpos)
        }
    }
}
