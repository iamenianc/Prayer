package au.prayer.app

import au.prayer.app.ui.navigation.LifoBackStack
import org.junit.Assert.*
import org.junit.Test

private enum class JournalSubView { ROOT, LIST, DETAIL, EDIT }

class LifoBackStackTest {

    @Test
    fun `test initial state of LifoBackStack`() {
        val stack = LifoBackStack("HOME")

        assertEquals(1, stack.size)
        assertEquals("HOME", stack.current)
        assertFalse("Initial stack with 1 element cannot pop", stack.canPop)
        assertEquals(listOf("HOME"), stack.items)
    }

    @Test
    fun `test push updates current and enables canPop`() {
        val stack = LifoBackStack("HOME")

        stack.push("JOURNAL")
        assertEquals(2, stack.size)
        assertEquals("JOURNAL", stack.current)
        assertTrue(stack.canPop)

        stack.push("PEOPLE")
        assertEquals(3, stack.size)
        assertEquals("PEOPLE", stack.current)
        assertTrue(stack.canPop)

        assertEquals(listOf("HOME", "JOURNAL", "PEOPLE"), stack.items)
    }

    @Test
    fun `test pop removes top in LIFO order and returns true`() {
        val stack = LifoBackStack("HOME")
        stack.push("JOURNAL")
        stack.push("PEOPLE")

        // Pop PEOPLE -> JOURNAL
        val popped1 = stack.pop()
        assertTrue(popped1)
        assertEquals("JOURNAL", stack.current)
        assertEquals(2, stack.size)
        assertTrue(stack.canPop)

        // Pop JOURNAL -> HOME
        val popped2 = stack.pop()
        assertTrue(popped2)
        assertEquals("HOME", stack.current)
        assertEquals(1, stack.size)
        assertFalse(stack.canPop)

        // Pop at root should return false and not remove root
        val popped3 = stack.pop()
        assertFalse(popped3)
        assertEquals("HOME", stack.current)
        assertEquals(1, stack.size)
    }

    @Test
    fun `test popToRoot returns immediately to base element`() {
        val stack = LifoBackStack("HOME")
        stack.push("JOURNAL")
        stack.push("PEOPLE")
        stack.push("ENTITY_DETAIL")
        stack.push("ADD_PRAYER")

        assertEquals(5, stack.size)
        assertEquals("ADD_PRAYER", stack.current)

        stack.popToRoot()

        assertEquals(1, stack.size)
        assertEquals("HOME", stack.current)
        assertFalse(stack.canPop)
    }

    @Test
    fun `test replace modifies top element without altering stack size`() {
        val stack = LifoBackStack("HOME")
        stack.push("STEP_1")
        assertEquals(2, stack.size)
        assertEquals("STEP_1", stack.current)

        stack.replace("STEP_1_REVISED")
        assertEquals(2, stack.size)
        assertEquals("STEP_1_REVISED", stack.current)
        assertEquals(listOf("HOME", "STEP_1_REVISED"), stack.items)

        stack.pop()
        assertEquals("HOME", stack.current)
    }

    @Test
    fun `test multi-tier hierarchical back stack navigation flow`() {
        // Simulates:
        // 1. Home
        // 2. Open Journal (Root Selection)
        // 3. Select People (Entity List)
        // 4. Select Sarah (Entity Detail)
        // 5. Tap + Add prayer point (Log Prayer)
        val mainStack = LifoBackStack(ScreenState.HOME)
        mainStack.push(ScreenState.JOURNAL)

        val journalStack = LifoBackStack(JournalSubView.ROOT)
        journalStack.push(JournalSubView.LIST)
        journalStack.push(JournalSubView.DETAIL)

        // Tap Add Prayer Point: pushes LogPrayer onto mainStack
        mainStack.push(ScreenState.LOG_PRAYER)
        assertEquals(ScreenState.LOG_PRAYER, mainStack.current)

        // Going back from LogPrayer: pops LogPrayer, returns to Journal
        assertTrue(mainStack.pop())
        assertEquals(ScreenState.JOURNAL, mainStack.current)

        // Journal sub-stack state is preserved at DETAIL!
        assertEquals(JournalSubView.DETAIL, journalStack.current)

        // Going back in Journal: pops DETAIL -> LIST
        assertTrue(journalStack.pop())
        assertEquals(JournalSubView.LIST, journalStack.current)

        // Going back in Journal: pops LIST -> ROOT
        assertTrue(journalStack.pop())
        assertEquals(JournalSubView.ROOT, journalStack.current)

        // At ROOT, journalStack.canPop is false, so mainStack pops JOURNAL -> HOME
        assertFalse(journalStack.canPop)
        assertTrue(mainStack.pop())
        assertEquals(ScreenState.HOME, mainStack.current)
        assertFalse(mainStack.canPop)
    }

    @Test
    fun `test library and volume reader back stack navigation flow`() {
        val stack = LifoBackStack(ScreenState.HOME)
        assertEquals(ScreenState.HOME, stack.current)

        // Navigate Home -> Library
        stack.push(ScreenState.LIBRARY)
        assertEquals(ScreenState.LIBRARY, stack.current)
        assertEquals(2, stack.size)

        // Select Calvin Volume: Library -> Volume Reader
        stack.push(ScreenState.VOLUME_READER)
        assertEquals(ScreenState.VOLUME_READER, stack.current)
        assertEquals(3, stack.size)

        // Exit Volume Reader: returns to Library
        assertTrue(stack.pop())
        assertEquals(ScreenState.LIBRARY, stack.current)
        assertEquals(2, stack.size)

        // Exit Library: returns to Home
        assertTrue(stack.pop())
        assertEquals(ScreenState.HOME, stack.current)
        assertEquals(1, stack.size)
        assertFalse(stack.canPop)
    }
}
