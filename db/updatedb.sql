-- Update the markable table with word information (makes a lot things faster)
update markables set document_id = words.doc_id from words where markables.head = word_id;
update markables set sentence_id = words.sentence_id from words where markables.head = word_id;
update markables m set content = array_to_string(array(select word from words w
where w.word_id>=m.firstword and w.word_id<=m.lastword), ' ');

-- Calculation of markable_features
update markables m set markable_features = (select wordfeatures from words w where w.word_id = m.head);
