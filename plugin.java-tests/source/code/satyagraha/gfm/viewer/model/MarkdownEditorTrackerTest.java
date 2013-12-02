package code.satyagraha.gfm.viewer.model;

import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import code.satyagraha.gfm.support.api.MarkdownFileNature;
import code.satyagraha.gfm.ui.api.PageEditorTracker;
import code.satyagraha.gfm.ui.impl.PageEditorTrackerDefault;
import code.satyagraha.gfm.viewer.model.api.MarkdownListener;
import code.satyagraha.gfm.viewer.model.impl.MarkdownEditorTrackerDefault;

@RunWith(MockitoJUnitRunner.class)
public class MarkdownEditorTrackerTest {

    @Mock
    private IWorkbenchPage workbenchPage;

    @Mock
    private MarkdownListener listener;

    @Mock
    private IEditorReference editorRef;

    @Mock
    private IEditorPart editorPart;

    @Mock
    private IEditorInput editorInput;

    @Mock
    private IFile editorIFile;

    @Mock
    private IPath editorPath;

    @Mock
    private MarkdownFileNature fileNature;

    private PageEditorTracker pageEditorTracker;

    private MarkdownEditorTrackerDefault editorTracker;

    private ArgumentCaptor<IPropertyListener> propertyListenerCaptor;

    @Test
    public void shouldNotifyOnEditorPartOpenedTrackableFile() throws Exception {
        // given
        given(editorRef.getPage()).willReturn(workbenchPage);
        given(editorRef.getEditor(true)).willReturn(editorPart);
        given(editorPart.getEditorInput()).willReturn(editorInput);
        propertyListenerCaptor = ArgumentCaptor.forClass(IPropertyListener.class);
        willDoNothing().given(editorPart).addPropertyListener(propertyListenerCaptor.capture());
        given(editorInput.getAdapter(IFile.class)).willReturn(editorIFile);
        given(editorIFile.getFullPath()).willReturn(editorPath);
        given(fileNature.isTrackableFile(editorIFile)).willReturn(true);

        pageEditorTracker = new PageEditorTrackerDefault(workbenchPage);
        editorTracker = new MarkdownEditorTrackerDefault(pageEditorTracker, fileNature);
        editorTracker.start();
        editorTracker.addListener(listener);

        // when
        pageEditorTracker.partOpened(editorRef);

        // then
        verify(listener).notifyEditorFile(editorIFile);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void shouldNotifyOnEditorPartClosed() throws Exception {
        // given
        shouldNotifyOnEditorPartOpenedTrackableFile();

        // when
        pageEditorTracker.partClosed(editorRef);

        // then
        // verify(listener).showIFile(null);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void shouldNotNotifyOnEditorPartOpenedNonTrackableFile() throws Exception {
        // given
        given(editorRef.getPart(true)).willReturn(editorPart);
        given(editorPart.getEditorInput()).willReturn(editorInput);
        given(editorInput.getAdapter(IFile.class)).willReturn(editorIFile);
        given(editorIFile.getFullPath()).willReturn(editorPath);
        given(fileNature.isTrackableFile(editorIFile)).willReturn(false);

        pageEditorTracker = new PageEditorTrackerDefault(workbenchPage);
        editorTracker = new MarkdownEditorTrackerDefault(pageEditorTracker, fileNature);
        editorTracker.start();
        editorTracker.addListener(listener);

        // when
        pageEditorTracker.partOpened(editorRef);

        // then
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void shouldNotifyOnEditorPartSaved() throws Exception {
        // given
        shouldNotifyOnEditorPartOpenedTrackableFile();

        IPropertyListener propertyListener = propertyListenerCaptor.getValue();
        assertNotNull(propertyListener);

        given(editorPart.isDirty()).willReturn(false);

        // when
        propertyListener.propertyChanged(editorPart, IEditorPart.PROP_DIRTY);

        // then
        verify(listener, times(2)).notifyEditorFile(editorIFile);
    }

}
